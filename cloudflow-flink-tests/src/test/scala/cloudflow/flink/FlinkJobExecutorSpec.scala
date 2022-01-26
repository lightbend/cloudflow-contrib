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

import scala.collection.immutable.Seq
import scala.concurrent._
import com.typesafe.config._
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import cloudflow.streamlets._
import cloudflow.flink.avro._
import cloudflow.flink.testkit._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent._
import org.scalatest.wordspec.AnyWordSpecLike
import org.apache.flink.runtime.client.JobCancellationException

class FlinkJobExecutorSpec extends AnyWordSpecLike with Matchers with ScalaFutures {
  val streamletDef =
    StreamletDefinition("appId", "appVersion", "FlinkIngress", "streamletClass", List(), List(), ConfigFactory.empty)
  @transient lazy val env = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val ctx = new FlinkStreamletContextImpl(streamletDef, env, ConfigFactory.empty)

  "FlinkJobExecutor" should {
    "complete on job cancellation" in {

      val readyPromise = Promise[Dun]()
      val completionPromise = Promise[Dun]()
      val completionFuture = completionPromise.future
      val logic = new FlinkStreamletLogic() {
        override def buildExecutionGraph = {
          val id = new org.apache.flink.api.common.JobID(0L, 0L)
          throw new JobCancellationException(id, "foo", new Exception())
        }
      }

      val execution = ClusterFlinkJobExecutor.execute(logic, ctx, readyPromise, completionPromise)
      execution.completed.futureValue shouldBe Dun
    }

    "complete on an exception where the cause is a job cancellation" in {

      val readyPromise = Promise[Dun]()
      val completionPromise = Promise[Dun]()
      val completionFuture = completionPromise.future
      val logic = new FlinkStreamletLogic() {
        override def buildExecutionGraph = {
          val id = new org.apache.flink.api.common.JobID(0L, 0L)
          throw new Exception("bla", new JobCancellationException(id, "foo", new Exception()))
        }
      }

      val execution = ClusterFlinkJobExecutor.execute(logic, ctx, readyPromise, completionPromise)
      execution.completed.futureValue shouldBe Dun
    }
    "fail on an exception where the cause is not a job cancellation" in {

      val readyPromise = Promise[Dun]()
      val completionPromise = Promise[Dun]()
      val completionFuture = completionPromise.future
      val logic = new FlinkStreamletLogic() {
        override def buildExecutionGraph = {
          val id = new org.apache.flink.api.common.JobID(0L, 0L)
          throw new Exception("bla", new Exception())
        }
      }

      val execution = ClusterFlinkJobExecutor.execute(logic, ctx, readyPromise, completionPromise)
      execution.completed.failed.futureValue
    }
  }
}
