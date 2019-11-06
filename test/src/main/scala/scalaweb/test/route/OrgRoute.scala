package scalaweb.test.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import helloscala.http.route.AbstractRoute
import scalaweb.model.OrgCreateReq
import scalaweb.model.OrgPageReq
import scalaweb.service.OrgService

class OrgRoute(orgService: OrgService) extends AbstractRoute {

  def route: Route = pathPrefix("org") {
    createRoute ~
    getRoute ~
    pageRoute
  }

  def createRoute: Route = pathPost("item") {
    import helloscala.http.JacksonSupport._
    entity(as[OrgCreateReq]) { req =>
      futureComplete(orgService.create(req), successCode = StatusCodes.Created)
    }
  }

  def getRoute: Route = pathGet("item" / IntNumber) { orgId =>
    futureComplete(orgService.getById(orgId))
  }

  private val pagePdm = ('code.?, 'name.?, 'status.as[Int].?, 'page.as[Int].?(1), 'size.as[Int].?(30))

  def pageRoute: Route = pathGet("page") {
    parameters(pagePdm).as(OrgPageReq) { req =>
      futureComplete(orgService.page(req))
    }
  }

}
