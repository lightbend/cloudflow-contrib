/*
 * Copyright (C) 2016-2021 Lightbend Inc. <https://www.lightbend.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloudflow.flink

import java.nio.file.Path

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, Promise }
import scala.util.{ Failure, Try }
import com.typesafe.config.{ Config, ConfigValueType }
import net.ceedubs.ficus.Ficus._
import org.apache.flink.api.common.JobExecutionResult
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.client.program.ProgramAbortException
import org.apache.flink.runtime.client.JobCancellationException
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.datastream.{ DataStreamSink, DataStream => JDataStream }
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.scala._
import cloudflow.streamlets.BootstrapInfo._
import cloudflow.streamlets._
import org.apache.flink.configuration.{ ConfigOptions, Configuration, RestOptions }
import org.apache.flink.core.fs.FileSystem

import cloudflow.streamlets.StreamletExecution

/**
 * Different strategy for execution of Flink jobs in local mode and in cluster
 */
sealed trait FlinkJobExecutor extends Serializable {
  // Is this a path that we want to use?
  val flinkRuntime = "cloudflow.runtimes.flink.config.flink"
  def streamletRuntimeConfigPath(streamletName: String) = s"cloudflow.streamlets.$streamletName.config"
  val enableLocalWeb = "local.web"
  def execute(
      logic: FlinkStreamletLogic,
      context: FlinkStreamletContext,
      readyPromise: Promise[Dun],
      completionPromise: Promise[Dun]): StreamletExecution
}

/**
 * Execution in blocking mode.
 */
object ClusterFlinkJobExecutor extends FlinkJobExecutor {
  def execute(
      logic: FlinkStreamletLogic,
      context: FlinkStreamletContext,
      readyPromise: Promise[Dun],
      completionPromise: Promise[Dun]): StreamletExecution = {
    val completionFuture = completionPromise.future
    Try {
      logic.executeStreamingQueries(context.env)
    }.fold(
      th =>
        th match {
          // rethrow for Flink to catch as Flink control flow depends on this
          case pax: ProgramAbortException =>
            throw pax
          case t: Throwable =>
            if (causeIsCancellation(t)) completionPromise.trySuccess(Dun) else completionPromise.tryFailure(th)
        },
      _ => completionPromise.trySuccess(Dun))

    new StreamletExecution {
      val readyFuture = readyPromise.future

      def completed: Future[Dun] = completionFuture
      def ready: Future[Dun] = readyFuture
      def stop(): Future[Dun] = ???
    }
  }

  def causeIsCancellation(t: Throwable): Boolean =
    t match {
      case _: JobCancellationException => true
      case _ =>
        if (t.getCause != null) {
          causeIsCancellation(t.getCause)
        } else
          false
    }
}

/**
 * Future based execution of Flink jobs on the sandbox
 */
object LocalFlinkJobExecutor extends FlinkJobExecutor {
  def execute(
      logic: FlinkStreamletLogic,
      context: FlinkStreamletContext,
      readyPromise: Promise[Dun],
      completionPromise: Promise[Dun]): StreamletExecution = {

    val jobResult = Future(logic.executeStreamingQueries(context.env))
    jobResult.recoverWith { case t: Throwable =>
      Future.failed(t)
    }
    new StreamletExecution {
      val readyFuture = readyPromise.future

      def completed: Future[Dun] =
        jobResult.map(_ => Dun)
      def ready: Future[Dun] = readyFuture
      def stop(): Future[Dun] = ???
    }
  }
}
