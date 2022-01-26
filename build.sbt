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
      (Test / sourceGenerators) += (Test / avroScalaGenerateSpecific).taskValue,
      Test / parallelExecution := false)

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
      addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.8.3"),
      addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.25"),
      addSbtPlugin("com.lightbend.cloudflow" % "sbt-cloudflow" % Dependencies.Versions.cloudflowVersion),
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

lazy val spark =
  Project(id = "cloudflow-spark", base = file("cloudflow-spark"))
    .enablePlugins(ScalafmtPlugin)
    .settings(Dependencies.sparkStreamlet)
    .settings(
      name := "contrib-spark",
      scalaVersion := Dependencies.Scala212,
      crossScalaVersions := Vector(Dependencies.Scala212),
      scalafmtOnCompile := true,
      (Test / sourceGenerators) += (Test / avroScalaGenerateSpecific).taskValue)

lazy val sparkTestkit =
  Project(id = "cloudflow-spark-testkit", base = file("cloudflow-spark-testkit"))
    .dependsOn(spark)
    .enablePlugins(ScalafmtPlugin)
    .settings(Dependencies.sparkTestkit)
    .settings(
      name := "contrib-spark-testkit",
      scalaVersion := Dependencies.Scala212,
      crossScalaVersions := Vector(Dependencies.Scala212),
      scalafmtOnCompile := true)

lazy val sparkTests =
  Project(id = "cloudflow-spark-tests", base = file("cloudflow-spark-tests"))
    .dependsOn(sparkTestkit)
    .enablePlugins(ScalafmtPlugin)
    .settings(Dependencies.sparkTests)
    .settings(
      name := "contrib-spark-tests",
      scalaVersion := Dependencies.Scala212,
      crossScalaVersions := Vector(Dependencies.Scala212),
      scalafmtOnCompile := true,
      (Test / sourceGenerators) += (Test / avroScalaGenerateSpecific).taskValue,
      Test / parallelExecution := false)

lazy val sparkSbtPlugin =
  Project(id = "cloudflow-sbt-spark", base = file("cloudflow-sbt-spark"))
    .settings(name := "sbt-cloudflow-contrib-spark")
    .enablePlugins(BuildInfoPlugin, ScalafmtPlugin, SbtPlugin)
    .settings(
      name := "contrib-sbt-spark",
      scalaVersion := Dependencies.Scala212,
      scalafmtOnCompile := true,
      sbtPlugin := true,
      crossSbtVersions := Vector("1.4.9"),
      buildInfoKeys := Seq[BuildInfoKey](version),
      addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.8.3"),
      addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.25"),
      addSbtPlugin("com.lightbend.cloudflow" % "sbt-cloudflow" % Dependencies.Versions.cloudflowVersion),
      scriptedLaunchOpts := {
        scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
      },
      scriptedDependencies := {
        (ThisProject / scriptedDependencies).value
        (spark / publishLocal).value
        (sparkTestkit / publishLocal).value
        (sparkTests / publishLocal).value
      },
      scriptedBufferLog := false)

lazy val cloudflowIt =
  Project(id = "cloudflow-it", base = file("cloudflow-it"))
    .configs(IntegrationTest.extend(Test))
    .settings(Defaults.itSettings, Dependencies.cloudflowIt)
    .settings(
      publish / skip := true,
      scalaVersion := Dependencies.Scala213,
      crossScalaVersions := Vector(Dependencies.Scala213),
      inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings),
      IntegrationTest / fork := true)

lazy val root = Project(id = "root", base = file("."))
  .settings(name := "root", publish / skip := true, scalafmtOnCompile := true)
  .withId("root")
  .aggregate(
    flink,
    flinkTestkit,
    flinkTests,
    flinkSbtPlugin,
    spark,
    sparkTestkit,
    sparkTests,
    sparkSbtPlugin,
    cloudflowIt)

lazy val flinkDocs = Project(id = "flink-docs", base = file("flink-docs"))
  .enablePlugins(ScalaUnidocPlugin)
  .settings(
    name := "flink-docs",
    publish / skip := true,
    scalafmtOnCompile := true,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(flink, flinkTestkit))
  .aggregate(flink, flinkTestkit)

lazy val sparkDocs = Project(id = "spark-docs", base = file("spark-docs"))
  .enablePlugins(ScalaUnidocPlugin)
  .settings(
    name := "spark-docs",
    publish / skip := true,
    scalafmtOnCompile := true,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(spark, sparkTestkit))
  .aggregate(spark, sparkTestkit)

lazy val setVersionFromTag = taskKey[Unit]("Set a stable version from env variable")

setVersionFromTag := {
  IO.write(
    file("version.sbt"),
    s"""ThisBuild / version := "${sys.env
      .get("VERSION")
      .getOrElse("0.0.0-SNAPSHOT")}"""")
}
