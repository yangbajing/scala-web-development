package akkahttp.server

import akka.http.scaladsl.server.{Directives, Route}

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-17.
 */
trait BaseRoute extends Directives {
  implicit def system = Server.theSystem

  implicit def mat = Server.materializer

  implicit def ec = Server.ec

  def route: Route
}
