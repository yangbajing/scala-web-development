import Common._
import Dependencies._

version in ThisBuild := "1.0.0"

shellPrompt := (s => Project.extract(s).currentProject.id + " > ")

scalafmtOnCompile in ThisBuild := true

lazy val root = Project("scala-web-root", file("."))
  .aggregate(
    test,
    oauth,
    foundation,
    database,
    common
  )

lazy val book = project
  .in(file("book"))
  .enablePlugins(ParadoxPlugin)
  .dependsOn(test, oauth, foundation, database, common)
  .settings(
    name in (Compile, paradox) := "Scala Web Development",
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    paradoxProperties ++= Map(
      "github.base_url" -> s"https://github.com/yangbajing/scala-web-development/tree/${version.value}",
      "extref.rfc.base_url" -> "http://tools.ietf.org/html/rfc%s",
      "image.base_url" -> ".../assets/imgs",
      "scala.version" -> scalaVersion.value,
      "scala.binary_version" -> scalaBinaryVersion.value,
      "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/$versionAkka",
      "akka.version" -> versionAkka
    )
  )

lazy val test = project
  .in(file("test"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      )
  )

lazy val oauth = project
  .in(file("oauth"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      )
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
