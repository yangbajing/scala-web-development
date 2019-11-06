package fusion.http.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route

trait AbstractRoute extends Directives {
  def route: Route

  val completeNotImplemented = complete(StatusCodes.NotImplemented)
}
