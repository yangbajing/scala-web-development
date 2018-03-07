package akkahttp.server

/**
  * Created by yangbajing(yangbajing@gmail.com) on 2017-04-17.
  */
object Server {
  implicit val theSystem: ActorSystem = ActorSystem("akka-http-foundation")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = theSystem.dispatcher
}
