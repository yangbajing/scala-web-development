package book.typed

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.scalatest.WordSpecLike

// #HelloScala-scala
object HelloScala {
  // #HelloScala
  sealed trait Command
  final case class Hello(message: String, replyTo: ActorRef[Reply]) extends Command
  final case class Tell(message: String) extends Command

  sealed trait Reply
  final case class HelloReply(message: String) extends Reply

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      case Hello(message, replyTo) =>
        replyTo ! HelloReply(s"$message, scala!")
        Behaviors.same
      case Tell(message) =>
        context.log.debug("收到消息：{}", message)
        Behaviors.same
    }
  }
  // #HelloScala
}

class HelloScalaSpec extends ScalaTestWithActorTestKit with WordSpecLike {
  import HelloScala._

  "HelloScala" should {
    // #HelloScalaSpec
    "tell" in {
      val actorRef = spawn(HelloScala(), "tell")
      actorRef ! Tell("Hello")
    }

    "replyTo" in {
      val actorRef = spawn(HelloScala(), "replyTo")
      val probe = createTestProbe[Reply]()
      actorRef ! Hello("hello", probe.ref)
      probe.expectMessageType[HelloReply] should be(HelloReply("hello, scala!"))
    }

    "ask" in {
      import akka.actor.typed.scaladsl.AskPattern._
      val actorRef = spawn(HelloScala(), "ask")
      val reply = actorRef.ask[Reply](replyTo => Hello("Hello", replyTo)).mapTo[HelloReply].futureValue
      reply.message should be("Hello, scala!")
    }
    // #HelloScalaSpec
  }

}
// #HelloScala-scala
