import sbt._

object Dependencies {
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.5"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"

  lazy val elastic4sTcp = "com.sksamuel.elastic4s" %% "elastic4s-tcp" % "5.3.1"
  lazy val alpakkaCassandra = ("com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "0.7").exclude("com.datastax.cassandra", "cassandra-driver-core")

  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

  lazy val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.7"
  lazy val jacksonDatatypeJsr310 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.8.7"
  lazy val jacksonDatatypeJdk8 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.8.7"

  lazy val postgresql = "org.postgresql" % "postgresql" % "42.0.0"
  lazy val cassandraDriverCore = "com.datastax.cassandra" % "cassandra-driver-core" % "3.2.0"
  lazy val cassandraDriverExtras = "com.datastax.cassandra" % "cassandra-driver-extras" % "3.2.0"

  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.7"

}
