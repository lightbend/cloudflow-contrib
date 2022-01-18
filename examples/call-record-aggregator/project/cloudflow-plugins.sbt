val currentVersion = {
  sys.env.get("CLOUDFLOW_CONTRIB_VERSION").fold(
    sbtdynver.DynVer(None, "-", "v")
      .getGitDescribeOutput(new java.util.Date())
      .fold(throw new Exception("Failed to retrieve version"))(_.version("-"))
  )(identity)
}

addSbtPlugin("com.lightbend.cloudflow" % "sbt-cloudflow" % "2.3.0-RC30")
addSbtPlugin("com.lightbend.cloudflow" % "contrib-sbt-spark" % currentVersion)
