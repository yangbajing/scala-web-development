name in Global := "multi-project"

organization in Global := "me.yangbajing"

version in Global := "0.0.1"

scalaVersion in Global := "2.12.8"

lazy val `multi-project-root` = project.in(file("."))
  .aggregate(app)

lazy val app = project
  .dependsOn(common)

lazy val common = project
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )
