import sbt.Keys._
import sbt._

object Dependencies {

  val Scala212 = "2.12.13"
  val Scala213 = "2.13.5"

  object Versions {
    val cloudflowVersion = "2.0.26-RC12"

    val flinkVersion = "1.13.0"
    val sparkVersion = "2.4.5"
    val akka = "2.6.14"
    val jackson = "2.11.4"
    val scalaTest = "3.2.7"
    val logbackVersion = "1.2.3"
  }

  object Compile {
    val cloudflowStreamlet = "com.lightbend.cloudflow" %% "cloudflow-streamlets" % Versions.cloudflowVersion

    val akkaActor = "com.typesafe.akka" %% "akka-actor" % Versions.akka
    val akkaStream = "com.typesafe.akka" %% "akka-stream" % Versions.akka
    val akkaProtobuf = "com.typesafe.akka" %% "akka-protobuf" % Versions.akka
    val akkaDiscovery = "com.typesafe.akka" %% "akka-discovery" % Versions.akka

    val flink = "org.apache.flink" %% "flink-scala" % Versions.flinkVersion
    val flinkStreaming = "org.apache.flink" %% "flink-streaming-scala" % Versions.flinkVersion
    val flinkAvro = "org.apache.flink" % "flink-avro" % Versions.flinkVersion
    val flinkKafka = "org.apache.flink" %% "flink-connector-kafka" % Versions.flinkVersion
    val flinkWeb = "org.apache.flink" %% "flink-runtime-web" % Versions.flinkVersion

    val spark = "org.apache.spark" %% "spark-core" % Versions.sparkVersion
    val sparkMllib = "org.apache.spark" %% "spark-mllib" % Versions.sparkVersion
    val sparkSql = "org.apache.spark" %% "spark-sql" % Versions.sparkVersion
    val sparkSqlKafka = "org.apache.spark" %% "spark-sql-kafka-0-10" % Versions.sparkVersion
    val sparkStreaming = "org.apache.spark" %% "spark-streaming" % Versions.sparkVersion
    val sparkProto = "com.thesamet.scalapb" %% "sparksql-scalapb" % "0.9.0"

    val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % Versions.jackson
    val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % Versions.jackson
    val jacksonScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jackson

    val log4jOverSlf4j = "org.slf4j" % "log4j-over-slf4j" % "1.7.30"
    val logbackClassic = "ch.qos.logback" % "logback-classic" % Versions.logbackVersion
    val logbackCore = "ch.qos.logback" % "logback-core" % Versions.logbackVersion

    val scalatest = "org.scalatest" %% "scalatest" % Versions.scalaTest
  }

  object TestDeps {

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
        Compile.scalatest % Test))

  val flinkTests =
    libraryDependencies ++= Seq(Compile.scalatest % Test, TestDeps.scalatestJunit, TestDeps.jodaTime)

  val sparkStreamlet = Seq(
    libraryDependencies ++= Seq(
        Compile.cloudflowStreamlet,
        Compile.akkaActor,
        Compile.akkaStream,
        Compile.akkaProtobuf,
        Compile.akkaDiscovery,
        Compile.log4jOverSlf4j,
        Compile.spark,
        Compile.sparkMllib,
        Compile.sparkSql,
        Compile.sparkSqlKafka,
        Compile.sparkStreaming,
        Compile.sparkProto,
        Compile.logbackClassic,
        Compile.logbackCore,
        Compile.scalatest % Test),
    libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-log4j12")) },
    dependencyOverrides ++= Seq(Compile.jacksonCore, Compile.jacksonDatabind, Compile.jacksonScala))

  val sparkTestkit = Seq(
    libraryDependencies ++= Seq(Compile.scalatest, TestDeps.scalatestJunit, TestDeps.jodaTime),
    dependencyOverrides ++= Seq(Compile.jacksonCore, Compile.jacksonDatabind, Compile.jacksonScala))

  val sparkTests =
    dependencyOverrides ++= Seq(Compile.jacksonCore, Compile.jacksonDatabind, Compile.jacksonScala)

}
