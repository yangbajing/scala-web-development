import sbt.Keys._
import sbt._

object Common {

  val basicSettings = Seq(
    organization := "me.yangbajing",
    organizationName := "Yangbajing's Garden",
    organizationHomepage := Some(url("https://www.yangbajing.me")),
    homepage := Some(url("https://www.yangbajing.me/scala-web-development/")),
    startYear := Some(2018),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    javacOptions ++= Seq(
      "-Xlint:deprecation"
    ),
    scalacOptions ++= Seq(
      "-encoding",
      "utf8",
      "-unchecked",
      "-feature",
      "-deprecation"
    ),
    javaOptions ++= Seq(
      "-Dnashorn.args=--language=es6"
    ),
    libraryDependencies ++= Seq(
      Dependencies._scalatest
    )
  )
}
