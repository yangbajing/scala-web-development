package helloscala.test

import akka.actor.ActorSystem
import akka.stream.Materializer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite

import scala.concurrent.Await
import scala.concurrent.duration._

trait AkkaSpec extends BeforeAndAfterAll {
  this: Suite =>

  protected def createActorSystem() = ActorSystem("AkkaTest")

  protected def createActorMaterializer()(implicit system: ActorSystem) =
    Materializer(system)

  implicit val system: ActorSystem = createActorSystem()

  implicit val materializer: Materializer = createActorMaterializer()

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
    Await.result(system.whenTerminated, 30.seconds)
  }
}
