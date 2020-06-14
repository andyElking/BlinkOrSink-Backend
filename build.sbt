lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """Blink-Or-Sink backend""",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.2",
    libraryDependencies ++= Seq(
      "com.google.inject" % "guice" % "4.2.3",
      "com.lihaoyi" %% "upickle" % "1.1.0"
      /*"com.h2database" % "h2" % "1.4.199",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test*/
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
