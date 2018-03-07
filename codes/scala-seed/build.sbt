import Dependencies._

lazy val `scala-seed` = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "me.yangbajing",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "scala-seed",
    libraryDependencies += scalaTest % Test
  )

