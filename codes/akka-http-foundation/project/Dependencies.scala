import sbt._

object Dependencies {
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.1.0"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"

  lazy val elastic4sTcp = "com.sksamuel.elastic4s" %% "elastic4s-tcp" % "6.2.3"
  lazy val alpakkaCassandra = ("com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "0.17")
    .excludeAll(ExclusionRule("com.datastax.cassandra"))

  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

  lazy val redisclient = "net.debasishg" %% "redisclient" % "3.5"

  lazy val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.4"
  lazy val jacksonDatatypeJsr310 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.4"
  lazy val jacksonDatatypeJdk8 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.9.4"

  lazy val postgresql = "org.postgresql" % "postgresql" % "42.2.1"
  lazy val cassandraDriverCore = "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.2"
  lazy val cassandraDriverExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.3.2"

  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

}

