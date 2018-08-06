import Common._

shellPrompt := { s =>
  Project.extract(s).currentProject.id + " > "
}

lazy val root = Project("scala-web-root", file("."))
  .aggregate(
    foundation,
    database,
    test,
    common
  )

lazy val foundation = project.in(file("foundation"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "net.debasishg" %% "redisclient" % "3.7",
      ("com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "0.20").excludeAll(ExclusionRule("com.datastax.cassandra")),
      "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.2",
      "com.datastax.cassandra" % "cassandra-driver-extras" % "3.3.2"
    )
  )

lazy val database = project.in(file("database"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
    )
  )

lazy val test = project.in(file("test"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(

    )
  )

lazy val common = project.in(file("common"))
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.3",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.3" % Test,
      "com.typesafe.akka" %% "akka-stream" % "2.5.14",
      "com.typesafe.slick" %% "slick" % "3.2.3",
      "com.github.tminglei" %% "slick-pg" % "0.16.3",
      "com.github.tminglei" %% "slick-pg_json4s" % "0.16.3",
      "com.typesafe.slick" %% "slick-testkit" % "3.2.3" % Test,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.6",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.9.6",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.6",
      "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % "2.9.6",
      "org.postgresql" % "postgresql" % "42.2.2",
      "com.zaxxer" % "HikariCP" % "3.2.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )