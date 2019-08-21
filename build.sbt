import Dependencies._


val Http4sVersion = "0.20.10"
val Json4sVersion ="3.1.0"

lazy val root = (project in file("."))
  .settings(
    organization in ThisBuild := "co.toplab",
    scalaVersion in ThisBuild := "2.12.7",
    version in ThisBuild := "0.1.0-SNAPSHOT",
    name := "Top_lab",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-twirl" % Http4sVersion,
      "org.json4s" %% "json4s-jackson" % "3.2.11",
      "org.json4s" %% "json4s-ext" % "3.2.11",
    ),
    libraryDependencies += scalaTest % Test
  )
