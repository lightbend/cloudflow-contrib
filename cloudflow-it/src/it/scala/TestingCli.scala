import akka.cli.cloudflow._
import akka.cli.cloudflow.commands.{ format, Command }
import akka.cli.cloudflow.kubeclient.KubeClientFabric8
import io.fabric8.kubernetes.client.KubernetesClient
import org.slf4j.LoggerFactory
import scala.util.Try

class TestingCli(val client: KubernetesClient, logger: CliLogger = new CliLogger(None))
    extends Cli(None, (_, _) => new KubeClientFabric8(None, _ => client)(logger))(logger) {

  val testLogger = LoggerFactory.getLogger(this.getClass)

  var lastResult: String = ""

  def exec(folder: String, cmd: String): Unit = {
    import scala.sys.process._
    val res = Process(
      Seq(
        "sh",
        "-c",
        "(PATH=$PATH:$PWD/flink/bin:$PWD/spark/bin && cd ../example-scripts/" + folder + " && " + cmd + ")")).!
    assert { res == 0 }
  }

  override def run[T](cmd: Command[T]): Try[T] = {
    cmd match {
      case _: commands.Undeploy =>
        exec("flink/undeploy", "./undeploy-application.sh swiss-knife")
        exec("spark-cli/undeploy", "./undeploy-application.sh swiss-knife")
      case _ =>
    }
    super.run(cmd)
  }

  def transform[T](cmd: Command[T], res: T): T = {
    cmd match {
      case _: commands.Deploy =>
        exec("flink/deploy", "./deploy-application.sh swiss-knife flink-service-account")
        exec("spark-cli/deploy", "./deploy-application.sh swiss-knife spark-service-account")
      case _: commands.Configure =>
        exec("flink/redeploy", "./redeploy-application.sh swiss-knife flink-service-account")
        exec("spark-cli/redeploy", "./redeploy-application.sh swiss-knife spark-service-account")
      case _ =>
    }

    val newResult = cmd.toString + "\n" + res.asInstanceOf[Result].render(format.Table)
    if (newResult != lastResult) {
      lastResult = newResult
      testLogger.debug(newResult)
    }
    res
  }

  def handleError[T](cmd: Command[T], ex: Throwable): Unit = {
    testLogger.warn(s"Error executing command ${cmd}", ex)
  }
}
