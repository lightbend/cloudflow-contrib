import sbt.Keys._
import sbt._

object Dependencies {

  val Scala212 = "2.12.13"
  val Scala213 = "2.13.5"

  object Versions {
    val cloudflowVersion = "2.0.26-RC12"

    val flinkVersion = "1.13.0"
    val scalaTest = "3.2.7"
    val logbackVersion = "1.2.3"
  }

  object Compile {
    val cloudflowStreamlet = "com.lightbend.cloudflow" %% "cloudflow-streamlets" % Versions.cloudflowVersion

    val flink = "org.apache.flink" %% "flink-scala" % Versions.flinkVersion
    val flinkStreaming = "org.apache.flink" %% "flink-streaming-scala" % Versions.flinkVersion
    val flinkAvro = "org.apache.flink" % "flink-avro" % Versions.flinkVersion
    val flinkKafka = "org.apache.flink" %% "flink-connector-kafka" % Versions.flinkVersion
    val flinkWeb = "org.apache.flink" %% "flink-runtime-web" % Versions.flinkVersion

    val logbackClassic = "ch.qos.logback" % "logback-classic" % Versions.logbackVersion
    val logbackCore = "ch.qos.logback" % "logback-core" % Versions.logbackVersion
  }

  object TestDeps {

    val scalatest = "org.scalatest" %% "scalatest" % Versions.scalaTest % Test
    val scalatestJunit = "org.scalatestplus" %% "junit-4-13" % s"${Versions.scalaTest}.0" % Test
    val jodaTime = "joda-time" % "joda-time" % "2.10.6"

  }

  val flinkStreamlet = Seq(
    resolvers ++= Seq("Flink 13.0 RC0".at("https://repository.apache.org/content/repositories/orgapacheflink-1417/")),
    libraryDependencies ++= Seq(
        Compile.cloudflowStreamlet,
        Compile.flink,
        Compile.flinkStreaming,
        Compile.flinkKafka,
        Compile.flinkAvro,
        Compile.flinkWeb,
        Compile.logbackClassic,
        Compile.logbackCore,
        TestDeps.scalatest))

  val flinkTests =
    libraryDependencies ++= Seq(TestDeps.scalatest, TestDeps.scalatestJunit, TestDeps.jodaTime)
}
