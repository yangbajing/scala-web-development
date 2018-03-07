package akkahttp.foundation.boot

import akka.http.scaladsl.Http
import akkahttp.foundation.route.Routes

import scala.util.{Failure, Success}
import akkahttp.server.Server._

/**
  * Created by yangbajing(yangbajing@gmail.com) on 2017-04-17.
  */
object Boot {

  def main(args: Array[String]): Unit = {
    val bindingFuture = Http().bindAndHandle(
      handler = new Routes().route,
      interface = "0.0.0.0",
      port = 9999)

    bindingFuture.onComplete {
      case Success(binding) ⇒
        println(s"Bind success: $binding")
      case Failure(cause) ⇒
        cause.printStackTrace()
        System.exit(-1)
    }
  }

}
