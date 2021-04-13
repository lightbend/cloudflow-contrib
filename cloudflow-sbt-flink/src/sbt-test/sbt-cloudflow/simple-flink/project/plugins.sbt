addSbtPlugin("com.lightbend.cloudflow" % "sbt-cloudflow" % "2.0.26-RC12")
sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("com.lightbend.cloudflow.contrib" % "sbt-cloudflow-contrib-flink" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}

libraryDependencies += "com.lihaoyi" %% "ujson" % "0.9.5"
