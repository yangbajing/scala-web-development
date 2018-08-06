package me.yangbajing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}

/**
  * Created by yangbajing(yangbajing@gmail.com) on 2017-04-19.
  */
trait MeSpec extends WordSpec with MustMatchers with OptionValues with EitherValues with ScalaFutures with BeforeAndAfterAll {
  implicit val system = ActorSystem("scalatest")
  implicit val mat = ActorMaterializer()
  implicit def ec = system.dispatcher

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(Span(90, Seconds), Span(50, Milliseconds))

  override protected def afterAll(): Unit = {
    system.terminate()
    super.afterAll()
  }

}
