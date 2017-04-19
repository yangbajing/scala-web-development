import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "me.yangbajing",
      scalaVersion := "2.12.1",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "akka-http-foundation",
    libraryDependencies ++= Seq(
      akkaHttp,
      jacksonModuleScala,
      jacksonDatatypeJsr310,
      jacksonDatatypeJdk8,
      postgresql,
      cassandraDriverCore,
      elastic4sTcp,
      scalaLogging,
      logbackClassic,
      scalaTest % Test
    )
  )
