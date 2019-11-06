package scalaweb.auth

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.management.scaladsl.AkkaManagement
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import scalaweb.auth.service.AuthService
import scalaweb.auth.web.route.AuthRoute

import scala.util.Failure
import scala.util.Success

object OAuthMain extends App with StrictLogging {
  implicit val system = ActorSystem("oauth")
  implicit val mat = Materializer(system)

  val management = AkkaManagement(system)
  management.start()
  sys.addShutdownHook { management.stop() }

  val host = "127.0.0.1"
  val port = 33333

  val service = new AuthService
  val handler = new AuthRoute(service).route

  Http()
    .bindAndHandle(handler, host, port)
    .onComplete {
      case Success(binding) =>
        logger.info(s"OAuth2 service startup success, $binding")
      case Failure(cause) =>
        logger.error("start failure", cause)
        system.terminate()
    }(system.dispatcher)
}
