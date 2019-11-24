package greeter

import akka.actor.typed.ActorSystem
import akka.actor.typed.SpawnProtocol
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.actor.typed.scaladsl.adapter._
import com.typesafe.scalalogging.StrictLogging

import scala.util.{ Failure, Success }

object GreeterApplication extends StrictLogging {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(SpawnProtocol(), "grpc")
    implicit val mat = Materializer(system)
    implicit val classicSystem = system.toClassic

    val handler = GreeterServiceHandler(new GreeterServiceImpl())
    val bindingF = Http().bindAndHandleAsync(handler, "localhost", 8000)
    bindingF.onComplete {
      case Success(binding) =>
        logger.info(s"Greeter gRPC server started, bind to $binding.")
      case Failure(e) =>
        logger.error(s"Greeter gRPC server start failed：${e.getMessage}, exit.")
        system.terminate()
    }(system.executionContext)
  }
}
