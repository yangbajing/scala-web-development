package fileupload.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class HtmlRoute {
  def route: Route = pathPrefix("file-upload") {
    getFromResourceDirectory("file-upload")
  }
}
