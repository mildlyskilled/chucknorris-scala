
name := "chuck-norris"

version := "1.0"

scalaVersion := "2.11.8"

lazy val root = (project in file(".")).
  enablePlugins(JavaAppPackaging).
  settings(
    name := "chuck-norris",
    scalaVersion := "2.11.8",
    version := "1.0"
  )

libraryDependencies ++= {
  lazy val akkaVersion = "2.4.9-RC2"
  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "org.json4s" %% "json4s-native" % "3.4.0"
  )
}