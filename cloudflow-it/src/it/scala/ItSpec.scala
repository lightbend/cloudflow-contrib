/*
 * Copyright (C) 2020 Lightbend Inc. <https://www.lightbend.com>
 */

import akka.cli.cloudflow._

import org.scalatest._
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.time.Span

import ItSetup._

trait ItSpec
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with Eventually
    with AppendedClues
    with ItMatchers
    with ItSetup {
  override implicit lazy val patienceConfig: PatienceConfig = PatienceConfig(resource.patience, resource.interval)
}

class ItGlobalSpec extends ItSpec with ItDeploySpec with ItBaseSpec with ItFrameworkConfigSpec with BeforeAndAfterAll {
  override def beforeAll() = {
    Setup.init()
    logger.debug("Init done")
    assumeClusterExists()
    logger.debug("Cluster exists, going to cleanup")
    undeployApp(failIfNotPresent = false)
  }

  override def afterAll() = {
    undeployApp()
  }
}

trait ItDeploySpec extends ItSpec {

  "Namespace setup" - {
    "should create namespace, pvcs and rbac settings" in {
      withK8s { k8s =>
        noException should be thrownBy k8s.namespaces().create(resource.namespace)
      }
      withK8s { k8s =>
        noException should be thrownBy loadResource(k8s, resource.pvcResourceSpark)
      }
      withK8s { k8s =>
        noException should be thrownBy loadResource(k8s, resource.pvcResourceFlink)
      }

      cli.exec("flink", "./setup-example-rbac.sh swiss-knife | true")
      cli.exec("spark-cli", "./setup-example-rbac.sh swiss-knife | true")
    }
  }

  "The application" - {
    "should deploy" in {
      val res = cli.run(commands.Deploy(crFile = resource.cr, confs = Seq(resource.defaultConfiguration)))
      assertSuccess(res)
    }

    "should be listed" in {
      eventually {
        val res = cli.run(commands.List())
        assertSuccess(res).withClue("List command failed.")
        (res.get.summaries.size shouldBe 1).withClue("Expected only 1 app.")
        (res.get.summaries.head.name shouldBe appName).withClue("Wrong app name.")
      }
    }

    "should eventually be 'Running'" in {
      eventually {
        val res = cli.run(commands.Status(appName))
        assertSuccess(res).withClue("Status command failed.")
        (res.get.status.summary.name shouldBe appName).withClue("Wrong app name.")
        (res.get.status.status shouldBe "Running").withClue("App not running.")
      }
    }

    "should undeploy" in {
      val res = cli.run(commands.Undeploy(appName))
      assertSuccess(res).withClue("Application undeploy failed.")
      eventually {
        val res = cli.run(commands.List())
        assertSuccess(res).withClue("List command failed.")
        (res.get.summaries.size shouldBe 0).withClue("App still listed.")
        withK8s { k8s =>
          (k8s.pods().inNamespace(appName).list().getItems().isEmpty() shouldBe true)
            .withClue(s"Pods for app ($appName) still exist.")
        }
      }
    }

    "should re-deploy to continue testing" in {
      val deploy = cli.run(commands.Deploy(crFile = resource.cr, confs = Seq(resource.defaultConfiguration)))
      assertSuccess(deploy)
      eventually {
        val res = cli.run(commands.Status(appName))
        (res.get.status.status shouldBe "Running").withClue("App not running.")
      }
    }
  }
}

trait ItBaseSpec extends ItSpec {
  "should contain these processes:" - {
    def check(proc: String) = withRunningApp { _ should containStreamlet(proc) }
    "spark" in check("spark-process")
    "flink" in check("flink-process")
  }

  "should write counter data to these output logs:" - {
    def check(streamlet: String) = withRunningApp { status =>
      eventually {
        streamletPodLog(status, streamlet) should include("count:")
      }
    }
    "raw" in check("raw-egress")
    "spark" in check("spark-egress")
    "flink" in check("flink-egress")
  }

  "is configurable" - {
    "reconfiguration should succeed" in {
      configureApp() { _ =>
        cli.run(
          commands.Configure(cloudflowApp = appName, confs = Seq(resource.updateConfig, resource.defaultConfiguration)))
      }
    }
    "reconfiguration should affect these streamlets:" - {
      def check(streamlet: String, wait: Span = resource.patience) = withRunningApp { status =>
        eventually(timeout(wait)) {
          streamletPodLog(status, streamlet) should include("payload: updated_config")
        }
      }
      "spark" in check("spark-egress")
      "flink" in check("flink-egress")
    }
  }

}

trait ItFrameworkConfigSpec extends ItSpec {
  "should reconfigure a spark application" in {
    note("reconfigure spark-specific configuration")
    configureApp() { _ =>
      cli.run(
        commands.Configure(
          cloudflowApp = appName,
          confs = Seq(resource.updateSparkConfiguration, resource.defaultConfiguration)))
    }

    note("verifying configuration update")
    withRunningApp { status =>
      eventually {
        matchingStreamletPodLog(status, "spark-config-output", "driver") should include("locality=[5s]")
      }
    }
  }
}
