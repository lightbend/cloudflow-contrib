ThisBuild / dynverSeparator := "-"

lazy val flink =
  Project(id = "cloudflow-flink", base = file("cloudflow-flink"))
    .enablePlugins(ScalafmtPlugin)
    .settings(Dependencies.flinkStreamlet)
    .settings(
      name := "contrib-flink",
      scalaVersion := Dependencies.Scala212,
      crossScalaVersions := Vector(Dependencies.Scala212),
      scalafmtOnCompile := true,
      libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-log4j12")) },
      Test / sourceGenerators += (Test / avroScalaGenerateSpecific).taskValue)

lazy val flinkTestkit =
  Project(id = "cloudflow-flink-testkit", base = file("cloudflow-flink-testkit"))
    .enablePlugins(ScalafmtPlugin)
    .dependsOn(flink)
    .settings(
      name := "contrib-flink-testkit",
      scalaVersion := Dependencies.Scala212,
      crossScalaVersions := Vector(Dependencies.Scala212),
      scalafmtOnCompile := true)

lazy val flinkTests =
  Project(id = "cloudflow-flink-tests", base = file("cloudflow-flink-tests"))
    .enablePlugins(JavaFormatterPlugin, ScalafmtPlugin)
    .dependsOn(flinkTestkit)
    .settings(Dependencies.flinkTests)
    .settings(
      name := "contrib-flink-tests",
      scalaVersion := Dependencies.Scala212,
      crossScalaVersions := Vector(Dependencies.Scala212),
      scalafmtOnCompile := true,
      (sourceGenerators in Test) += (avroScalaGenerateSpecific in Test).taskValue,
      parallelExecution in Test := false)

lazy val flinkSbtPlugin =
  Project(id = "cloudflow-sbt-flink", base = file("cloudflow-sbt-flink"))
    .settings(name := "sbt-cloudflow-contrib-flink")
    .enablePlugins(BuildInfoPlugin, ScalafmtPlugin, SbtPlugin)
    .settings(
      name := "contrib-sbt-flink",
      scalaVersion := Dependencies.Scala212,
      scalafmtOnCompile := true,
      sbtPlugin := true,
      crossSbtVersions := Vector("1.4.9"),
      buildInfoKeys := Seq[BuildInfoKey](version),
      addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.8.2"),
      addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.25"),
      scriptedLaunchOpts := {
        scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
      },
      scriptedDependencies := {
        (ThisProject / scriptedDependencies).value
        (flink / publishLocal).value
        (flinkTestkit / publishLocal).value
        (flinkTests / publishLocal).value
      },
      scriptedBufferLog := false)

lazy val root = Project(id = "root", base = file("."))
  .settings(name := "root", skip in publish := true, scalafmtOnCompile := true, crossScalaVersions := Seq())
  .withId("root")
  .aggregate(flink, flinkTestkit, flinkTests, flinkSbtPlugin)
