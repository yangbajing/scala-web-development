package book.typed

import akka.actor.typed._
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.TimerScheduler
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.IntStatus
import helloscala.common.util.AkkaUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Millis
import org.scalatest.time.Seconds
import org.scalatest.time.Span

import scala.concurrent.Await
import scala.concurrent.duration._

object DeepActor {

  sealed trait Command
  trait ControlCommand extends Command { val clientId: String }
  final case class Connect(clientId: String, replyTo: ActorRef[Reply]) extends Command with ControlCommand
  final case class Disconnect(clientId: String, replyTo: ActorRef[Reply]) extends Command with ControlCommand
  final case class AskMessage(clientId: String, message: String, replyTo: ActorRef[Reply]) extends Command
  final case class ConnectCount(replyTo: ActorRef[Reply]) extends Command
  final case class PublishEvent(clientId: String, event: String, payload: String) extends Command

  sealed trait Reply { val status: Int }
  final case class Connected(status: Int, clientId: String) extends Reply
  final case class Disconnected(status: Int, clientId: String) extends Reply
  final case class MessageAsked(status: Int, clientId: String, reply: String) extends Reply
  final case class ConnectCounted(count: Int, status: Int = IntStatus.OK) extends Reply
  final case class ReplyError(status: Int, clientId: String) extends Reply

  val serviceKey = ServiceKey[Command]("deep")

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.system.receptionist ! Receptionist.Register(serviceKey, context.self)
    new DeepActor(context).receive()
  }
}

final class DeepActor(context: ActorContext[DeepActor.Command]) {
  import DeepActor._
  private var connects = Map.empty[String, ActorRef[Command]]

  def receive(): Behavior[Command] =
    Behaviors
      .receiveMessage[Command] {
        case cmd @ Connect(clientId, replyTo) =>
          if (connects.contains(clientId)) {
            replyTo ! Connected(IntStatus.CONFLICT, clientId)
          } else {
            val child = context.spawn(DeepClient(clientId, context.self.narrow[ControlCommand]), clientId)
            context.watch(child)
            connects = connects.updated(clientId, child)
            child ! cmd
          }
          Behaviors.same

        case cmd @ Disconnect(clientId, replyTo) =>
          if (connects.contains(clientId)) {
            connects(clientId) ! cmd
          } else {
            replyTo ! Disconnected(IntStatus.NOT_FOUND, clientId)
          }
          Behaviors.same

        case cmd: AskMessage =>
          connects.get(cmd.clientId) match {
            case Some(ref) => ref ! cmd
            case None      => cmd.replyTo ! ReplyError(IntStatus.NOT_FOUND, cmd.clientId)
          }
          Behaviors.same

        case event: PublishEvent =>
          connects.get(event.clientId).foreach(_ ! event)
          Behaviors.same

        case ConnectCount(replyTo) =>
          replyTo ! ConnectCounted(connects.size)
          Behaviors.same
      }
      .receiveSignal {
        case (_, Terminated(child)) =>
          val clientId = child.path.name
          connects -= clientId
          context.unwatch(child)
          Behaviors.same
      }

}

object DeepClient {
  import DeepActor._

  def apply(clientId: String, parent: ActorRef[ControlCommand]): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.withTimers(timers => new DeepClient(clientId, parent, timers, context).init())
  }
}

final class DeepClient private (
    clientId: String,
    parent: ActorRef[DeepActor.ControlCommand],
    timers: TimerScheduler[DeepActor.Command],
    context: ActorContext[DeepActor.Command]) {
  import DeepActor._

  def active(): Behavior[Command] = Behaviors.receiveMessagePartial {
    case AskMessage(_, message, reply) =>
      reply ! MessageAsked(IntStatus.OK, clientId, message.reverse)
      Behaviors.same

    case PublishEvent(_, event, payload) =>
      context.log.debug("Receive event: {}, payload: {}", event, payload)
      Behaviors.same

    case Disconnect(_, replyTo) =>
      replyTo ! Disconnected(IntStatus.OK, clientId)
      Behaviors.stopped
  }

  def init(): Behavior[Command] = Behaviors.receiveMessage {
    case Connect(`clientId`, replyTo) =>
      replyTo ! Connected(IntStatus.OK, clientId)
      active()
    case other =>
      context.log.warn("Receive invalid command: {}", other)
      Behaviors.same
  }

}

class DeepActorSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with OptionValues
    with BeforeAndAfterAll
    with StrictLogging {

  implicit private val system = ActorSystem(SpawnProtocol(), "deep-manager")
  implicit private val timeout = Timeout(2.seconds)

  implicit override def patienceConfig: PatienceConfig = PatienceConfig(Span(10, Seconds), Span(50, Millis))

  "DeepActor" should {
    var deepActor: ActorRef[DeepActor.Command] = null

    "Create actor from outside of ActorSystem[_]" in {
      deepActor = system
        .ask[ActorRef[DeepActor.Command]](replTo => SpawnProtocol.Spawn(DeepActor(), "deep", Props.empty, replTo))
        .futureValue
      deepActor.path.name shouldBe "deep"
    }

    "Discover actors using ServiceKey[T]" in {
      val maybeDeepActor = AkkaUtils.findActorByServiceKey(DeepActor.serviceKey, 500.millis).futureValue
      val ref = maybeDeepActor.value
      ref shouldBe deepActor
    }

    val client1 = "client1"

    "Connect" in {
      val connected =
        deepActor.ask[DeepActor.Reply](DeepActor.Connect(client1, _)).mapTo[DeepActor.Connected].futureValue
      connected.status should be(IntStatus.OK)
      connected.clientId should be(client1)

      val connectCounted =
        deepActor.ask[DeepActor.Reply](DeepActor.ConnectCount).mapTo[DeepActor.ConnectCounted].futureValue
      connectCounted.count should be > 0
    }

    "AskMessage" in {
      val messageAsked = deepActor
        .ask[DeepActor.Reply](DeepActor.AskMessage(client1, "hello", _))
        .mapTo[DeepActor.MessageAsked]
        .futureValue
      messageAsked should be(DeepActor.MessageAsked(IntStatus.OK, client1, "olleh"))
    }

    "Disconnect" in {
      val disconnected = deepActor
        .ask[DeepActor.Reply](replyTo => DeepActor.Disconnect(client1, replyTo))
        .mapTo[DeepActor.Disconnected]
        .futureValue
      disconnected.status should be(IntStatus.OK)
      disconnected.clientId should be(client1)

      val connectCounted =
        deepActor.ask[DeepActor.Reply](DeepActor.ConnectCount).mapTo[DeepActor.ConnectCounted].futureValue
      connectCounted.count should be(0)
    }

    "AskMessage return 404" in {
      val messageAsked = deepActor
        .ask[DeepActor.Reply](DeepActor.AskMessage(client1, "hello", _))
        .mapTo[DeepActor.ReplyError]
        .futureValue
      messageAsked.status should be(IntStatus.NOT_FOUND)
    }

  }

  override protected def afterAll(): Unit = {
    system.terminate()
    Await.result(system.whenTerminated, 10.seconds)
  }
}
