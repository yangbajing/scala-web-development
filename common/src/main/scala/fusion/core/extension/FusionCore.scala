package fusion.core.extension

import akka.actor.ExtendedActorSystem
import akka.actor.typed._
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.PropsAdapter
import akka.actor.typed.scaladsl.adapter._
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.Configuration
import helloscala.common.exception.HSInternalErrorException

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._

trait FusionExtension extends Extension {
  val system: ActorSystem[_]
  def configuration: Configuration = FusionCore(system).configuration
}

abstract class FusionExtensionId[T <: Extension] extends ExtensionId[T]

class FusionCore private (system: ActorSystem[_]) extends Extension with StrictLogging {
  def name: String = system.name
  import system.executionContext
  implicit private val scheduler: Scheduler = system.scheduler

  logger.info("FusionCore instanced!")

  private lazy val _configuration = new Configuration(system.settings.config)

  def configuration: Configuration = _configuration

  val spawn: ActorRef[SpawnProtocol.Command] = {
    system.toClassic
      .actorOf(
        PropsAdapter(Behaviors.supervise(SpawnProtocol()).onFailure[RuntimeException](SupervisorStrategy.resume)),
        "spawn")
      .toTyped[SpawnProtocol.Command]
  }

  def classicSystem: ExtendedActorSystem = system.toClassic match {
    case v: ExtendedActorSystem => v
    case _                      => throw new IllegalStateException("Need ExtendedActorSystem instance.")
  }

  def spawnActor[REF](behavior: Behavior[REF], name: String, props: Props)(
      implicit timeout: Timeout): Future[ActorRef[REF]] = {
    spawn.ask(SpawnProtocol.Spawn(behavior, name, props, _))
  }

  def spawnActor[REF](behavior: Behavior[REF], name: String)(implicit timeout: Timeout): Future[ActorRef[REF]] =
    spawnActor(behavior, name, Props.empty)

  def spawnActorSync[REF](behavior: Behavior[REF], name: String, duration: FiniteDuration): ActorRef[REF] = {
    implicit val timeout = Timeout(duration)
    Await.result(spawnActor(behavior, name), duration)
  }

  def spawnActorSync[REF](
      behavior: Behavior[REF],
      name: String,
      props: Props,
      duration: FiniteDuration): ActorRef[REF] = {
    implicit val timeout = Timeout(duration)
    Await.result(spawnActor(behavior, name, props), duration)
  }

  def receptionistFind[T](serviceKey: ServiceKey[T], timeout: FiniteDuration)(
      func: Receptionist.Listing => ActorRef[T]): ActorRef[T] = {
    implicit val t: Timeout = Timeout(timeout)
    val f = system.receptionist
      .ask[Receptionist.Listing] { replyTo =>
        Receptionist.Find(serviceKey, replyTo)
      }
      //      .map { case ConfigManager.ConfigManagerServiceKey.Listing(refs) => refs.head }
      .map(func)
    Await.result(f, timeout)
  }

  def receptionistFindOne[T](serviceKey: ServiceKey[T], timeout: FiniteDuration): ActorRef[T] = {
    receptionistFindSet(serviceKey, timeout).headOption
      .getOrElse(throw HSInternalErrorException(s"$serviceKey not found!"))
  }

  def receptionistFindSet[T](serviceKey: ServiceKey[T], timeout: FiniteDuration): Set[ActorRef[T]] = {
    implicit val t: Timeout = Timeout(timeout)
    val f = system.receptionist.ask[Receptionist.Listing](Receptionist.Find(serviceKey)).map { listing =>
      logger.debug(s"receptionistFindSet($listing)")
      if (listing.isForKey(serviceKey)) {
        listing.serviceInstances(serviceKey)
      } else {
        Set[ActorRef[T]]()
      }
    }
    Await.result(f, timeout)
  }

}

object FusionCore extends ExtensionId[FusionCore] {
  override def createExtension(system: ActorSystem[_]): FusionCore = new FusionCore(system)
}
