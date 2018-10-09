package scalaweb.ant.design.pro

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import scalaweb.ant.design.pro.route.Routes

import scala.util.{Failure, Success}

// #main
object Main extends App with StrictLogging {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val bindingFuture = Http().bindAndHandle(handler = new Routes().route, interface = "0.0.0.0", port = 22222)

  bindingFuture.onComplete {
    case Success(binding) ⇒
      sys.addShutdownHook(system.terminate())
      logger.info(s"启动Akka HTTP Server成功，绑定地址: $binding")
    case Failure(e) ⇒
      logger.error(s"启动Akka HTTP Server失败：${e.getMessage}", e)
      system.terminate()
  }

}
// #main
