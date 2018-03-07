import Dependencies._

lazy val `akka-http-foundation` = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "me.yangbajing",
      scalaVersion := "2.12.4",
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
      cassandraDriverExtras,
      redisclient,
      scalaLogging,
      logbackClassic,
      scalaTest % Test
    )
  )
