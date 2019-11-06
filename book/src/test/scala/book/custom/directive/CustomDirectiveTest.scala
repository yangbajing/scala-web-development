package book.custom.directive

import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.FunSuite
import org.scalatest.MustMatchers
import akka.http.scaladsl.server.Directives._

class CustomDirectiveTest extends FunSuite with MustMatchers with ScalatestRouteTest {

  val twoIntParameters: Directive[(Int, Int)] =
    parameters(("a".as[Int], "b".as[Int]))

  val myDirective: Directive1[String] =
    twoIntParameters.tmap {
      case (a, b) => (a + b).toString
    }

  test("testCustomDirective") {
    Get("/?a=2&b=5") ~> myDirective(x => complete(x)) ~> check {
      responseAs[String] mustBe "7"
    }
  }

}
