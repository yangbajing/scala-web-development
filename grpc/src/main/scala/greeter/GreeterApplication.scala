package greeter

import akka.actor.typed.ActorSystem
import akka.actor.typed.SpawnProtocol
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.actor.typed.scaladsl.adapter._

object GreeterApplication {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(SpawnProtocol(), "grpc")
    implicit val mat = Materializer(system)
    implicit val classicSystem = system.toClassic

    val handler = GreeterServiceHandler(new GreeterServiceImpl())
    Http().bindAndHandleAsync(handler, "localhost", 8000)
  }
}
