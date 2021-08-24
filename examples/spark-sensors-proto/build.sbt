import sbt._
import sbt.Keys._
import sbtdocker.Instructions

lazy val sparkSensors = Project(id = "spark-sensors-proto", base = file("."))
    .enablePlugins(CloudflowApplicationPlugin, CloudflowSparkPlugin, CloudflowNativeSparkPlugin, ScalafmtPlugin)
    .settings(
      scalafmtOnCompile := true,
      libraryDependencies ++= Seq(
        "ch.qos.logback" %  "logback-classic" % "1.2.3",
        "org.scalatest"  %% "scalatest"       % "3.0.8" % "test"
      ),

      organization := "com.lightbend.cloudflow",
      headerLicense := Some(HeaderLicense.ALv2("(C) 2016-2020", "Lightbend Inc. <https://www.lightbend.com>")),

      baseDockerInstructions := sparkNativeCloudflowDockerInstructions.value,
      extraDockerInstructions := Seq(
        Instructions.Run.shell(Seq("rm", "/opt/cloudflow/protobuf-java*.jar"))
      ),
      libraryDependencies ~= fixSparkNativeCloudflowDeps,
      dependencyOverrides ++= Seq(
        "com.lightbend.akka.grpc" %% "akka-grpc-runtime" % "1.0.3",
      ),
      libraryDependencies += "com.thesamet.scalapb" %% "sparksql-scalapb" % "0.11.0",
      assemblyShadeRules in assembly := Seq(
        ShadeRule.rename("com.google.protobuf.**" -> "shadeproto.@1").inAll,
        ShadeRule.rename("scala.collection.compat.**" -> "shadecompat.@1").inAll
      ),
      assembly / assemblyOption := (assembly / assemblyOption).value, 
      Compile / packageBin := assembly.value,

      scalaVersion := "2.12.11",
      crossScalaVersions := Vector(scalaVersion.value),
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
