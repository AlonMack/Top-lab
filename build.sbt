import Dependencies._

lazy val circeVersion = "0.5.1"
lazy val http4sVersion = "0.14.6"

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
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.mongodb.scala" %% "mongo-scala-driver" % "2.7.0",
      "org.slf4j" % "slf4j-simple" % "1.7.28",
      scalaTest % Test
    )
  )