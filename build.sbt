import Dependencies._

lazy val http4sVersion = "0.14.6"
lazy val mongoDriverVersion = "2.7.0"

lazy val root = (project in file("."))
  .settings(
    organization in ThisBuild := "co.toplab",
    scalaVersion in ThisBuild := "2.11.8",
    version in ThisBuild := "0.1.0-SNAPSHOT",
    name := "Top_lab",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-json4s-native" % http4sVersion,
      "org.mongodb.scala" %% "mongo-scala-driver" % mongoDriverVersion,
      scalaTest % Test
    )
  )