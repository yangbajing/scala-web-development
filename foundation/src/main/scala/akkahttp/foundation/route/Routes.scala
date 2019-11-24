package akkahttp.foundation.route

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-17.
 */
class Routes {
  def route =
    path("hello") {
      get {
        complete(
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            "<h1>Say hello to akka-http</h1>"))
      }
    } ~
    new PageRoute().route
}
