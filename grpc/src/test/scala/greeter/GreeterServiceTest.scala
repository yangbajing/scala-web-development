package greeter

import java.util.concurrent.TimeUnit

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Sink, Source }
import org.scalatest.WordSpecLike

class GreeterServiceTest extends ScalaTestWithActorTestKit with WordSpecLike {
  "GreeterService" must {
    val greeterService = new GreeterServiceImpl()
    "sayHello" in {
      greeterService.sayHello(HelloRequest("Scala")).futureValue should ===(
        HelloReply("Hello, Scala."))
    }

    "itKeepsReplying" in {
      greeterService
        .itKeepsReplying(HelloRequest("Scala"))
        .take(5)
        .runWith(Sink.seq)
        .futureValue should ===(
        Seq(
          HelloReply("Hello, Scala; this is 1 times."),
          HelloReply("Hello, Scala; this is 2 times."),
          HelloReply("Hello, Scala; this is 3 times."),
          HelloReply("Hello, Scala; this is 4 times."),
          HelloReply("Hello, Scala; this is 5 times.")))
    }

    "itKeepsTalking" in {
      val (queue, in) =
        Source
          .queue[HelloRequest](16, OverflowStrategy.backpressure)
          .preMaterialize()
      val f = greeterService.itKeepsTalking(in)
      Seq("Scala", "Java", "Groovy", "Kotlin").foreach(program =>
        queue.offer(HelloRequest(program)))
      TimeUnit.SECONDS.sleep(1)
      queue.complete()
      f.futureValue should ===(HelloReply("Hello, Scala, Java, Groovy, Kotlin."))
    }

    "streamHellos" in {
      val (queue, in) =
        Source
          .queue[HelloRequest](16, OverflowStrategy.backpressure)
          .preMaterialize()
      val f = greeterService.streamHellos(in).runWith(Sink.seq)
      Seq("Scala", "Java", "Groovy", "Kotlin").foreach(item =>
        queue.offer(HelloRequest(item)))
      TimeUnit.SECONDS.sleep(1)
      queue.complete()
      f.futureValue should ===(
        Seq(
          HelloReply("Hello, Scala."),
          HelloReply("Hello, Java."),
          HelloReply("Hello, Groovy."),
          HelloReply("Hello, Kotlin.")))
    }
  }
}
