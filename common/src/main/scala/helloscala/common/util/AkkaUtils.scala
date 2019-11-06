package helloscala.common.util

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future
import scala.concurrent.duration._

object AkkaUtils extends StrictLogging {

  def findActorByServiceKey[T](
      serviceKey: ServiceKey[T],
      timeout: FiniteDuration,
      interval: FiniteDuration = 15.millis)(implicit system: ActorSystem[_]): Future[Option[ActorRef[T]]] = {
    import system.executionContext
    implicit val to = Timeout(interval)
    val total = if (interval > timeout) 1 else timeout.toMicros / interval.toMicros

    def _find(count: Int): Future[Option[ActorRef[T]]] = {
      val f = findActorByServiceKey(serviceKey).recover { case _: TimeoutException => None }
      f.flatMap {
        case None if count < total =>
          TimeUnit.MICROSECONDS.sleep(interval.toMicros)
          _find(count + 1)
        case None =>
          logger.error(s"findActorByServiceKey error：$count/$total ($serviceKey, $timeout, $interval).")
          Future.successful(None)
        case some =>
          logger.debug(s"findActorByServiceKey find：$count/$total ($serviceKey, $timeout, $interval).")
          Future.successful(some)
      }
    }

    _find(1)
  }

  private def findActorByServiceKey[T](
      serviceKey: ServiceKey[T])(implicit system: ActorSystem[_], timeout: Timeout): Future[Option[ActorRef[T]]] = {
    system.receptionist
      .ask[Receptionist.Listing](Receptionist.Find(serviceKey))
      .map { listing =>
        if (listing.isForKey(serviceKey)) listing.serviceInstances(serviceKey).headOption
        else None
      }(system.executionContext)
      .mapTo[Option[ActorRef[T]]]
  }

}
