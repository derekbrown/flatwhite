name := "flatwhite"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.0-SNAPSHOT",
  "ws.securesocial" %% "securesocial" % "master-SNAPSHOT"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
