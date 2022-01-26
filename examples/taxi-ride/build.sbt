import sbt._
import sbt.Keys._
import cloudflow.sbt.CommonSettingsAndTasksPlugin._

lazy val root =
  Project(id = "root", base = file("."))
    .enablePlugins(ScalafmtPlugin)
    .settings(
      name := "root",
      publish / skip := true,
      scalafmtOnCompile := true,
    )
    .withId("root")
    .settings(commonSettings)
    .aggregate(
      taxiRidePipeline,
      datamodel,
      ingestor,
      processor,
      ridelogger
    )

lazy val taxiRidePipeline = appModule("taxi-ride-pipeline")
  .enablePlugins(CloudflowApplicationPlugin)
  .settings(commonSettings)
  .settings(
    name := "taxi-ride-fare",
    runLocalConfigFile := Some("taxi-ride-pipeline/src/main/resources/local.conf"),
    runLocalLog4jConfigFile := Some("taxi-ride-pipeline/src/main/resources/log4j.properties")
  )

lazy val datamodel = appModule("datamodel")
  .settings(
    commonSettings,
    Compile / sourceGenerators += (Compile / avroScalaGenerateSpecific).taskValue,
    libraryDependencies += Cloudflow.library.CloudflowAvro
  )

lazy val ingestor = appModule("ingestor")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka"         %% "akka-http-spray-json"   % "10.1.12",
      "ch.qos.logback"            %  "logback-classic"        % "1.2.10",
      "org.scalatest"             %% "scalatest"              % "3.0.8"    % "test"
    )
  )
  .dependsOn(datamodel)


lazy val processor = appModule("processor")
  .enablePlugins(CloudflowNativeFlinkPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
        "ch.qos.logback"         %  "logback-classic"        % "1.2.10",
        "org.scalatest"          %% "scalatest"              % "3.0.8"  % "test"
      ),
    dependencyOverrides ++= Seq(
     "org.apache.kafka" % "kafka-clients" % "3.0.0",
     "org.apache.commons" % "commons-compress" % "1.21"
    ),
    parallelExecution in Test := false
  )
  .dependsOn(datamodel)

lazy val ridelogger = appModule("logger")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback"         %  "logback-classic"        % "1.2.10",
      "org.scalatest"          %% "scalatest"              % "3.0.8"    % "test"
    )
  )
  .dependsOn(datamodel)

def appModule(moduleID: String): Project = {
  Project(id = moduleID, base = file(moduleID))
    .settings(
      name := moduleID
    )
    .withId(moduleID)
    .settings(commonSettings)
}

lazy val commonSettings = Seq(
  organization := "com.lightbend.cloudflow",
  headerLicense := Some(HeaderLicense.ALv2("(C) 2016-2020", "Lightbend Inc. <https://www.lightbend.com>")),
  scalaVersion := "2.12.15",
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked"
  ),
  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused-import"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
)

dynverSeparator in ThisBuild := "-"
