addSbtPlugin("com.lightbend.cloudflow" % "sbt-cloudflow" % "2.3.1-RC1")
sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("com.lightbend.cloudflow" % "contrib-sbt-flink" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}

libraryDependencies += "com.lihaoyi" %% "ujson" % "0.9.5"
