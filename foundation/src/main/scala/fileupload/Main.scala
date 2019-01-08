package fileupload

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import fileupload.controller.{FileRoute, HtmlRoute}
import fileupload.service.FileService
import akka.http.scaladsl.server.Directives._

import scala.util.{Failure, Success}

object Main extends StrictLogging {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val mat = ActorMaterializer()

    val host = "127.0.0.1"
    val port = 33333

    val fileService = FileService(system, mat)
    val handler = new FileRoute(fileService).route ~
      new HtmlRoute().route

    val bindingFuture = Http().bindAndHandle(handler, host, port)

    bindingFuture.onComplete {
      case Success(binding) =>
        logger.info(s"startup success, $binding")
      case Failure(cause) =>
        logger.error("start failure", cause)
        system.terminate()
    }(system.dispatcher)
  }

}
