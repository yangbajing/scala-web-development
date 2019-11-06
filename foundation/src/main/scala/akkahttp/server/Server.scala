package akkahttp.server

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.Materializer

import scala.concurrent.ExecutionContextExecutor

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-17.
 */
object Server {
  implicit val theSystem: ActorSystem = ActorSystem("akka-http-foundation")
  implicit val materializer = Materializer(theSystem)
  implicit val ec: ExecutionContextExecutor = theSystem.dispatcher
}
