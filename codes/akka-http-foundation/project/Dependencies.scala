import sbt._

object Dependencies {
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.5"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"

  lazy val elastic4sTcp = "com.sksamuel.elastic4s" %% "elastic4s-tcp" % "5.3.1"

  lazy val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.7"
  lazy val jacksonDatatypeJsr310 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.8.7"
  lazy val jacksonDatatypeJdk8 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.8.7"

  lazy val postgresql = "org.postgresql" % "postgresql" % "42.0.0"
  lazy val cassandraDriverCore = "com.datastax.cassandra" % "cassandra-driver-core" % "3.2.0"

}
