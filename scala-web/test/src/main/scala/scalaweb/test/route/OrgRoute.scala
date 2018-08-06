package scalaweb.test.route

import akka.http.scaladsl.server.Directives._
import scalaweb.model.OrgCreateReq
import scalaweb.service.OrgService

class OrgRoute(orgService: OrgService) {

  def route= pathPrefix("route") {
    createRoute
  }

  def createRoute = (path("item") & post) {
    import com.helloscala.jackson.JacksonSupport._
    entity(as[OrgCreateReq]) { req =>
      onSuccess(orgService.create(req)) { resp =>
        complete(resp)
      }
    }
  }
}
