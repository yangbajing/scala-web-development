package example.route

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

class TestRoutesTest extends WordSpecLike with ScalatestRouteTest with Matchers {

  val textParam: Directive1[String] = parameter("text".as[String])
  val lengthDirective: Directive1[Int] = textParam.map(text => text.length)

  val twoIntParameters: Directive[(Int, Int)] = parameters(("a".as[Int], "b".as[Int]))

  val myDirective: Directive1[String] = twoIntParameters.tmap {
    case (a, b) => (a + b).toString
  }

  val route = pathPrefix("api") { ctx =>
    println(ctx.unmatchedPath)
    ctx.complete(ctx.request.uri.path.toString())

  }

  "Routes Test" should {
    "ctx.unmatchedPath" in {
      Get("/api/user/page") ~> route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[String]
        println(resp)
      }
    }

    "test" in {
      mapRequest(request => request.withHeaders(request.headers :+ RawHeader("custom-key", "custom-value")))
      path("api" / "user" / "page")
      mapRouteResultPF {
        case RouteResult.Rejected(_) =>
          RouteResult.Complete(HttpResponse(StatusCodes.InternalServerError))
      }
      extract(ctx => ctx.request.uri)
    }
  }

}
