import Common._
import Dependencies._

shellPrompt := (s =>  Project.extract(s).currentProject.id + " > ")

scalafmtOnCompile in ThisBuild := true

lazy val root = Project("scala-web-root", file("."))
  .aggregate(
    test,
    foundation,
    database,
    common
  )

lazy val test = project
  .in(file("test"))
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
