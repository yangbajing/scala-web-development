import sbt._

object Dependencies {
  val versionScala = "2.13.1"
  val versionScalaLib = "2.13"
  val versionScalaCollectionCompat = "2.1.2"

  val _scalaXml = ("org.scala-lang.modules" %% "scala-xml" % "1.2.0").exclude("org.scala-lang", "scala-library")

  val _scalaJava8Compat =
    ("org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0").exclude("org.scala-lang", "scala-library")

  val _scalaCollectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % versionScalaCollectionCompat

  val _scalatest = "org.scalatest" %% "scalatest" % "3.0.8"

  val versionAkka = "2.6.0-RC2"
  lazy val _akkaRemote = "com.typesafe.akka" %% "akka-remote" % versionAkka

  lazy val _akkas = Seq(
    "com.typesafe.akka" %% "akka-slf4j" % versionAkka,
    "com.typesafe.akka" %% "akka-stream-typed" % versionAkka,
    "com.typesafe.akka" %% "akka-serialization-jackson" % versionAkka,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % versionAkka % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % versionAkka % Test)
    .map(_.exclude("org.scala-lang.modules", s"scala-java8-compat").cross(CrossVersion.binary))

  lazy val _akkaPersistence = "com.typesafe.akka" %% "akka-persistence-query" % versionAkka

  lazy val _akkaMultiNodeTestkit = "com.typesafe.akka" %% "akka-multi-node-testkit" % versionAkka % Test

  lazy val _akkaClusters = Seq(
    "com.typesafe.akka" %% "akka-cluster-typed" % versionAkka,
    //"com.typesafe.akka" %% "akka-cluster-metrics" % versionAkka,
    "com.typesafe.akka" %% "akka-cluster-sharding-typed" % versionAkka,
    _akkaMultiNodeTestkit)

  val versionAkkaHttp = "10.1.10"

  val _akkaHttpCore = ("com.typesafe.akka" %% "akka-http-core" % versionAkkaHttp)
    .exclude("com.typesafe.akka", "akka-stream")
    .cross(CrossVersion.binary)

  val _akkaHttpTestkit = ("com.typesafe.akka" %% "akka-http-testkit" % versionAkkaHttp)
    .exclude("com.typesafe.akka", "akka-stream")
    .cross(CrossVersion.binary)

  val _akkaHttps = Seq(
    "com.typesafe.akka" %% "akka-http" % versionAkkaHttp,
    "com.typesafe.akka" %% "akka-http-spray-json" % versionAkkaHttp)
    .map(_.exclude("com.typesafe.akka", "akka-stream").cross(CrossVersion.binary))

  lazy val _akkaManagements = Seq(
    "com.lightbend.akka.management" %% "akka-management" % "1.0.3",
    "com.lightbend.akka.management" %% "akka-management-cluster-http" % "1.0.3")
    .map(_.excludeAll(ExclusionRule("com.typesafe.akka")).exclude("org.scala-lang", "scala-library"))

  private val versionAlpakka = "1.1.2"

  val _alpakkaSimpleCodecs =
    ("com.lightbend.akka" %% "akka-stream-alpakka-simple-codecs" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaXml =
    ("com.lightbend.akka" %% "akka-stream-alpakka-xml" % versionAlpakka).excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaCsv =
    ("com.lightbend.akka" %% "akka-stream-alpakka-csv" % versionAlpakka).excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaJsonStreaming =
    ("com.lightbend.akka" %% "akka-stream-alpakka-json-streaming" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaFile =
    ("com.lightbend.akka" %% "akka-stream-alpakka-file" % versionAlpakka).excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaFtp =
    ("com.lightbend.akka" %% "akka-stream-alpakka-ftp" % versionAlpakka).excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaUnixDomainSocket =
    ("com.lightbend.akka" %% "akka-stream-alpakka-unix-domain-socket" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaMongodb =
    ("com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaCassandra =
    ("com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"), ExclusionRule("com.datastax.cassandra"))

  val _alpakkaElasticsearch =
    ("com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaHbase =
    ("com.lightbend.akka" %% "akka-stream-alpakka-hbase" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakksHdfs =
    ("com.lightbend.akka" %% "akka-stream-alpakka-hdfs" % versionAlpakka).excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkaText =
    ("com.lightbend.akka" %% "akka-stream-alpakka-text" % versionAlpakka).excludeAll(ExclusionRule("com.typesafe.akka"))

  val _alpakkas = Seq(
    _alpakkaText,
    _alpakkaSimpleCodecs,
    _alpakkaXml,
    _alpakkaCsv,
    _alpakkaJsonStreaming,
    _alpakkaFile,
    _alpakkaFtp,
    _alpakkaUnixDomainSocket)

  val _alpakkaNoSQLs = Seq(
    _alpakkaMongodb,
    _alpakkaCassandra,
    //                           _alpakkaHbase,
    //                           _alpakksHdfs,
    _alpakkaElasticsearch)

  private val versionAkkaPersistenceCassandra = "0.88"

  val _akkaPersistenceCassandras = Seq(
    "com.typesafe.akka" %% "akka-persistence-cassandra" % versionAkkaPersistenceCassandra,
    "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % versionAkkaPersistenceCassandra % Test)

  val _akkaStreamKafka = ("com.typesafe.akka" %% "akka-stream-kafka" % "1.1.0")
    .exclude("com.typesafe.akka", "akka-slf4j")
    .cross(CrossVersion.binary)

  val _akkaHttpJackson = "de.heikoseeberger" %% "akka-http-jackson" % "1.29.1"

  val _config = "com.typesafe" % "config" % "1.3.3"

  val _hanlp = "com.hankcs" % "hanlp" % "portable-1.6.6"

  val _sjsonnet = "com.lihaoyi" %% "sjsonnet" % "0.1.6"

  val _requests = "com.lihaoyi" %% "requests" % "0.2.0"

  val _redisclient = "net.debasishg" %% "redisclient" % "3.10"

  val _aspectjweaver = "org.aspectj" % "aspectjweaver" % "1.9.1"

  val _sigarLoader = "io.kamon" % "sigar-loader" % "1.6.6" //-rev002"

  val _kamonCore = "io.kamon" %% "kamon-core" % "2.0.1"

  val _kamonAkka = ("io.kamon" %% "kamon-akka" % "2.0.0")
    .excludeAll(ExclusionRule("com.typesafe.akka"))
    .cross(CrossVersion.binary)
    .exclude("org.scala-lang", "scala-library")

  val _kamonAkkaHttp = ("io.kamon" %% "kamon-akka-http" % "2.0.2")
    .exclude("io.kamon", "kamon-akka")
    .cross(CrossVersion.binary)
    .exclude("com.typesafe.akka", "akka-http")
    .cross(CrossVersion.binary)
    .exclude("com.typesafe.akka", "akka-stream")
    .cross(CrossVersion.binary)
    .exclude("org.scala-lang", "scala-library")

  // need aspectjweaver

  val _kamonSystemMetrics = ("io.kamon" %% "kamon-system-metrics" % "2.0.0")
    .exclude("io.kamon", "kamon-core")
    .cross(CrossVersion.binary)
    .exclude("org.scala-lang", "scala-library")

  val _kamons = Seq(_kamonCore, _kamonAkka, _kamonAkkaHttp, _kamonSystemMetrics)

  val _scopt = "com.github.scopt" %% "scopt" % "3.7.0"

  private val versionCats = "2.0.0"

  val _catses = Seq("org.typelevel" %% "cats-laws", "org.typelevel" %% "cats-free").map(_ % versionCats)

  //  private val versionCirce = "0.9.3"
  //  val _circes = Seq(
  //    "io.circe" %% "circe-core",
  //    "io.circe" %% "circe-generic",
  //    "io.circe" %% "circe-parser",
  //    "io.circe" %% "circe-java8"
  //  ).map(_ % versionCirce)

  val versionSlick = "3.3.2"

  val _slicks = Seq(
    "com.typesafe.slick" %% "slick" % versionSlick,
    "com.typesafe.slick" %% "slick-testkit" % versionSlick % Test,
    "com.github.tminglei" %% "slick-pg" % "0.18.0",
    "com.github.tminglei" %% "slick-pg_json4s" % "0.18.0")

  val _jwtCore = "com.pauldijou" %% "jwt-core" % "4.1.0"

  private val versionPoi = "3.17"
  val _pois = Seq("org.apache.poi" % "poi-scratchpad" % versionPoi, "org.apache.poi" % "poi-ooxml" % versionPoi)

  private val versionCassandra = "3.7.2"

  val _cassandraDrivers = Seq(
    "com.datastax.cassandra" % "cassandra-driver-core" % versionCassandra,
    "com.datastax.cassandra" % "cassandra-driver-extras" % versionCassandra)

  val _logs =
    Seq("com.typesafe.scala-logging" %% "scala-logging" % "3.9.2", "ch.qos.logback" % "logback-classic" % "1.2.3")

  val _rhino = "org.mozilla" % "rhino" % "1.7.10"

  private val versionGuice = "4.1.0"
  val _guice = "com.google.inject" % "guice" % versionGuice
  val _guiceAssistedinject = "com.google.inject.extensions" % "guice-assistedinject" % versionGuice

  val _h2 = "com.h2database" % "h2" % "1.4.200"

  val _bouncycastleProvider = "org.bouncycastle" % "bcprov-jdk15on" % "1.64"

  private val versionQuartz = "2.2.3"
  val _quartz = "org.quartz-scheduler" % "quartz" % versionQuartz

  val _postgresql = "org.postgresql" % "postgresql" % "42.2.8"

  val _mysql = "mysql" % "mysql-connector-java" % "8.0.18"

  val _mssql = "com.microsoft.sqlserver" % "mssql-jdbc" % "6.4.0.jre8"

  val _hikariCP = "com.zaxxer" % "HikariCP" % "3.4.1"

  val _scalapb = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"

  val _swaggerAkkaHttp = "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.0.4"

  val _commonsVfs = "org.apache.commons" % "commons-vfs2" % "2.2"

  val _jsch = "com.jcraft" % "jsch" % "0.1.54"

}
