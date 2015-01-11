name := "flatwhite"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka22",
"ws.securesocial" %% "securesocial" % "2.1.4"
)

play.Project.playScalaSettings
