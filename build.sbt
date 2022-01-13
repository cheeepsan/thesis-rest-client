name := "rest-integration"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies += "com.typesafe" % "config" % "1.4.1"

val circeVersion = "0.14.1"

val akkaVersion = "2.6.14"
val AkkaHttpVersion = "10.2.6"
libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-json-streaming" % "3.0.3",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
)

libraryDependencies ++= Seq(
  "com.norbitltd" %% "spoiwo" % "2.0.0"
)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)