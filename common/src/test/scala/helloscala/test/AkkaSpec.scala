package helloscala.test

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite

import scala.concurrent.Await
import scala.concurrent.duration._

trait AkkaSpec extends BeforeAndAfterAll {
  this: Suite =>

  protected def createActorSystem() = ActorSystem("AkkaTest")

  protected def createActorMaterializer()(implicit system: ActorSystem) = ActorMaterializer()

  implicit val system: ActorSystem = createActorSystem()

  implicit val materializer: ActorMaterializer = createActorMaterializer()

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
    Await.result(system.whenTerminated, 30.seconds)
  }

}
