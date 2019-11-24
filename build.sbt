import Common._
import Dependencies._

ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := versionScala

ThisBuild / scalafmtOnCompile := true

ThisBuild / shellPrompt := (s => Project.extract(s).currentProject.id + " > ")

lazy val root = Project("scala-web-development", file(".")).aggregate(
  test,
  `config-discovery`,
  `engineering-guice`,
  oauth,
  grpc,
  monitor,
  data,
  foundation,
  database,
  common)

lazy val book = project
  .in(file("book"))
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(
    `config-discovery`,
    `engineering-guice`,
    grpc,
    monitor,
    test,
    oauth,
    foundation,
    database,
    common % "compile->compile;test->test")
  .settings(
    name in (Compile, paradox) := "Scala Web Development",
    Compile / paradoxMaterialTheme ~= {
      _.withLanguage(java.util.Locale.SIMPLIFIED_CHINESE)
        .withColor("teal", "indigo")
        .withRepository(uri("https://github.com/yangbajing/scala-web-development"))
        .withSocial(
          uri("https://github.com/yangbajing"),
          uri("https://weibo.com/yangbajing"),
          uri("https://www.yangbajing.me/"))
    },
    paradoxProperties ++= Map(
        "github.base_url" -> s"https://github.com/yangbajing/scala-web-development/tree/${version.value}",
        "extref.rfc.base_url" -> "http://tools.ietf.org/html/rfc%s",
        "image.base_url" -> ".../assets/imgs",
        "scala.version" -> scalaVersion.value,
        "scala.binary_version" -> scalaBinaryVersion.value,
        "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/$versionAkka",
        "akka.version" -> versionAkka),
    libraryDependencies ++= Seq(_akkaHttpTestkit))

lazy val test = project
  .in(file("test"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= _slicks)

lazy val `config-discovery` = project
  .in(file("config-discovery"))
  .enablePlugins(AkkaGrpcPlugin, JavaAgent)
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "runtime;test",
    mainClass in assembly := Some("scalaweb.discovery.server.Application"),
    libraryDependencies ++= Seq(_scalapb, _akkaPersistence) ++ _akkaClusters)

lazy val `ant-design-pro` = project
  .in(file("ant-design-pro"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    mainClass in assembly := Some("scalaweb.ant.design.pro.Main"),
//    test in assembly := {},
    libraryDependencies ++= Seq(_rhino))

lazy val `engineering-guice` = project
  .in(file("engineering-guice"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    mainClass in assembly := Some("scalaweb.enginnering.guice.boot.Main"),
    //    test in assembly := {},
    libraryDependencies ++= Seq(_guice, _guiceAssistedinject))

lazy val monitor = project
  .in(file("monitor"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    mainClass in assembly := Some("scalaweb.monitor.boot.Main"),
    //    test in assembly := {},
    libraryDependencies ++= Seq() ++ _kamons)

lazy val grpc = project
  .in(file("grpc"))
  .enablePlugins(AkkaGrpcPlugin, JavaAgent)
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    javaAgents += _alpnAgent % "runtime;test",
    libraryDependencies ++= Seq(
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"))

lazy val data = project
  .in(file("data"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= Seq(_akkaHttpJackson))

lazy val oauth = project
  .in(file("oauth"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(_jwtCore) ++ _akkaClusters ++ _akkaManagements)

lazy val foundation = project
  .in(file("foundation"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(_redisclient, _alpakkaCassandra) ++ _cassandraDrivers)

lazy val database = project
  .in(file("database"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(libraryDependencies ++= Seq())

lazy val common = project
  .in(file("common"))
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
        _bouncycastleProvider,
        _postgresql,
        _hikariCP,
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
        _config,
        _akkaHttpTestkit % Test,
        _scalaCollectionCompat,
        _scalaJava8Compat) ++ _akkas ++ _akkaHttps ++ _logs,
    PB.targets in Compile := Seq(
        scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value))
