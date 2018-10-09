package akkahttp.foundation.route

import akka.http.scaladsl.server.Route
import akkahttp.foundation.data.domain.PageInput
import helloscala.http.JacksonSupport._
import akkahttp.server.BaseRoute

class PageRoute extends BaseRoute {

  def route: Route =
    path("page") {
      post {
        entity(as[PageInput]) { pageInput =>
          complete(pageInput)
        }
      }
    }

}
