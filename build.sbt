lazy val flink =
  Project(id = "cloudflow-flink", base = file("cloudflow-flink"))
    .enablePlugins(ScalafmtPlugin)
    .settings(Dependencies.flinkStreamlet)
    .settings(
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
      scalaVersion := Dependencies.Scala212,
      crossScalaVersions := Vector(Dependencies.Scala212),
      scalafmtOnCompile := true)

lazy val flinkTests =
  Project(id = "cloudflow-flink-tests", base = file("cloudflow-flink-tests"))
    .enablePlugins(JavaFormatterPlugin, ScalafmtPlugin)
    .dependsOn(flinkTestkit)
    .settings(Dependencies.flinkTests)
    .settings(
      scalaVersion := Dependencies.Scala212,
      crossScalaVersions := Vector(Dependencies.Scala212),
      scalafmtOnCompile := true,
      (sourceGenerators in Test) += (avroScalaGenerateSpecific in Test).taskValue,
      parallelExecution in Test := false)
