val currentVersion = {
  sys.env.get("CLOUDFLOW_CONTRIB_VERSION").fold(
    sbtdynver.DynVer(None, "-", "v")
      .getGitDescribeOutput(new java.util.Date())
      .fold(throw new Exception("Failed to retrieve version"))(_.version("-"))
  )(identity)
}

addSbtPlugin("com.lightbend.cloudflow" % "sbt-cloudflow" % "2.3.0-RC11-2-db8b5209-20220113-2111")
addSbtPlugin("com.lightbend.cloudflow" % "contrib-sbt-flink" % currentVersion)
addSbtPlugin("com.lightbend.cloudflow" % "contrib-sbt-spark" % currentVersion)
