import Common._
import Dependencies._

version in Global := "1.0.0"

shellPrompt := (s => Project.extract(s).currentProject.id + " > ")

scalafmtOnCompile in Global := true

lazy val root = Project("scala-web-root", file("."))
  .aggregate(
    test,
    `engineering-guice`,
    oauth,
    monitor,
    data,
    foundation,
    database,
    common
  )

lazy val book = project
  .in(file("book"))
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(`engineering-guice`, monitor, test, oauth, foundation, database, common)
  .settings(
    name in (Compile, paradox) := "Scala Web Development",
    Compile / paradoxMaterialTheme ~= {
      _.withLanguage(java.util.Locale.SIMPLIFIED_CHINESE)
        .withColor("teal", "indigo")
        .withRepository(uri("https://github.com/yangbajing/scala-web-development"))
        .withSocial(
          uri("https://github.com/yangbajing"),
          uri("https://weibo.com/yangbajing"),
          uri("https://www.yangbajing.me/")
        )
    },
    paradoxProperties ++= Map(
      "github.base_url" -> s"https://github.com/yangbajing/scala-web-development/tree/${version.value}",
      "extref.rfc.base_url" -> "http://tools.ietf.org/html/rfc%s",
      "image.base_url" -> ".../assets/imgs",
      "scala.version" -> scalaVersion.value,
      "scala.binary_version" -> scalaBinaryVersion.value,
      "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/$versionAkka",
      "akka.version" -> versionAkka
    ),
    libraryDependencies ++= Seq(
      _akkaHttpTestkit
    )
  )

lazy val test = project
  .in(file("test"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)

lazy val `ant-design-pro` = project
  .in(file("ant-design-pro"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    mainClass in assembly := Some("scalaweb.ant.design.pro.Main"),
//    test in assembly := {},
    libraryDependencies ++= Seq(
      _rhino
    )
  )

lazy val `engineering-guice` = project
  .in(file("engineering-guice"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    mainClass in assembly := Some("scalaweb.enginnering.guice.boot.Main"),
    //    test in assembly := {},
    libraryDependencies ++= Seq(
      _guice,
      _guiceAssistedinject
    )
  )

lazy val monitor = project
  .in(file("monitor"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    mainClass in assembly := Some("scalaweb.monitor.boot.Main"),
    //    test in assembly := {},
    libraryDependencies ++= Seq(
      _sjsonnet
    ) ++ _kamons
  )

lazy val data = project
  .in(file("data"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      _chillAkka,
      _akkaHttpJackson
    )
  )

lazy val oauth = project
  .in(file("oauth"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      _jwtCore
    ) ++ _akkaClusters ++ _akkaManagements
  )

lazy val foundation = project
  .in(file("foundation"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      _redisclient,
      _alpakkaCassandra
    ) ++ _cassandraDrivers
  )

lazy val database = project
  .in(file("database"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      )
  )

lazy val common = project
  .in(file("common"))
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      _bouncycastleProvider,
      _postgresql,
      _hikariCP,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      _config
    ) ++ _akkas ++ _akkaHttps ++ _slicks ++ _jsons ++ _logs,
    PB.targets in Compile := Seq(
      scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
    )
  )
