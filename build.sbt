import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "mdp.grp11",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "MDP",
    libraryDependencies += scalaTest % Test
)

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.11"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.9"
libraryDependencies += "com.google.code.gson" % "gson" % "1.7.1"
