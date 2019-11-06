/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.discovery.server.config

import java.util.UUID

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ddata.ORMap
import akka.cluster.ddata.SelfUniqueAddress
import akka.cluster.ddata.typed.scaladsl.DistributedData
import akka.cluster.ddata.typed.scaladsl.Replicator
import akka.stream.QueueOfferResult.QueueClosed
import akka.stream.scaladsl.SourceQueueWithComplete
import fusion.discovery.model.ConfigChanged
import fusion.discovery.server.config.data.ConfigContent
import fusion.discovery.server.config.data.NamespaceKey
import fusion.json.jackson.CborSerializable
import helloscala.common.IntStatus

import scala.collection.immutable
import scala.concurrent.duration._

object Configs {
  val READ_TIMEOUT = 2.seconds
  val WRITE_TIMEOUT = 5.seconds

  type ReplicatorType = ORMap[String, ConfigContent]

  sealed trait Command extends CborSerializable

  case class GetContent(dataIds: immutable.Seq[String], replyTo: ActorRef[immutable.Seq[ConfigContent]]) extends Command

  case class UpdateContent(dataId: String, content: String, replyTo: ActorRef[ModifyReply]) extends Command
  case class RemoveContent(dataId: String, replyTo: ActorRef[ModifyReply]) extends Command
  case class AllContent(replyTo: ActorRef[immutable.Seq[ConfigContent]]) extends Command

  case class RegisterListener(listenerId: UUID, dataId: String, queue: SourceQueueWithComplete[ConfigChanged])
      extends Command

  private case class RemoveListener(listenerId: UUID) extends Command

  sealed private trait InternalCommand extends Command

  private case class InternalSubscribeResponse(chg: Replicator.SubscribeResponse[ReplicatorType])
      extends InternalCommand

  private case class InternalGetResponse(
      chg: Replicator.GetResponse[ReplicatorType],
      dataIds: immutable.Seq[String],
      replyTo: ActorRef[immutable.Seq[ConfigContent]])
      extends InternalCommand

  private case class InternalAllResponse(
      chg: Replicator.GetResponse[ReplicatorType],
      replyTo: ActorRef[immutable.Seq[ConfigContent]])
      extends InternalCommand

  private case class InternalUpdateResponse(
      chg: Replicator.UpdateResponse[ReplicatorType],
      replyTo: ActorRef[ModifyReply])
      extends InternalCommand

  private case class InternalRemoveResponse(
      chg: Replicator.UpdateResponse[ReplicatorType],
      replyTo: ActorRef[ModifyReply])
      extends InternalCommand

  sealed trait Reply extends CborSerializable
  case class ModifyReply(status: Int, message: String) extends Reply

  object ModifyReply {
    def ok = ModifyReply(IntStatus.OK, "")
    def error(msg: String) = ModifyReply(IntStatus.ERROR, msg)
  }

  def apply(namespaceKey: NamespaceKey): Behavior[Command] = Behaviors.setup { context =>
    implicit val node: SelfUniqueAddress = DistributedData(context.system).selfUniqueAddress

    DistributedData.withReplicatorMessageAdapter[Command, ReplicatorType] { replicatorAdapter =>
      replicatorAdapter.subscribe(namespaceKey, InternalSubscribeResponse.apply)

      def active(listeners: List[RegisterListener], cachedConfigs: Map[String, ConfigContent]): Behavior[Command] = {
        Behaviors.receiveMessagePartial[Command] {
          case GetContent(dataIds, replyTo) =>
            val isCachedExists = cachedConfigs.nonEmpty && cachedConfigs.keysIterator.forall(key =>
                dataIds.contains(key))
            context.log.debug(s"isCachedExists: $isCachedExists")
            if (isCachedExists) {
              replyTo ! dataIds.flatMap(sn => cachedConfigs.get(sn))
              Behaviors.same
            } else {
              replicatorAdapter.askGet(
                Replicator.Get(namespaceKey, Replicator.ReadMajority(READ_TIMEOUT)),
                chg => InternalGetResponse(chg, dataIds, replyTo))
              Behaviors.same
            }

          case UpdateContent(dataId, content, replyTo) =>
            replicatorAdapter.askUpdate(
              Replicator.Update(namespaceKey, ORMap.empty[String, ConfigContent], Replicator.WriteAll(WRITE_TIMEOUT)) {
                orMap =>
                  orMap :+ (dataId -> ConfigContent(namespaceKey.namespace, dataId, 0, content))
              },
              chg => InternalUpdateResponse(chg, replyTo))
            Behaviors.same

          case RemoveContent(dataId, replyTo) =>
            replicatorAdapter.askUpdate(
              askReplyTo =>
                Replicator.Update(
                  namespaceKey,
                  ORMap.empty[String, ConfigContent],
                  Replicator.WriteMajority(WRITE_TIMEOUT),
                  askReplyTo)(orMap => orMap.remove(dataId)),
              chg => InternalRemoveResponse(chg, replyTo))
            Behaviors.same

          case AllContent(replyTo) =>
            replicatorAdapter.askGet(
              Replicator.Get(namespaceKey, Replicator.ReadMajority(READ_TIMEOUT)),
              chg => InternalAllResponse(chg, replyTo))
            Behaviors.same

          case internal: InternalCommand =>
            receiveInternalCommand(listeners, cachedConfigs, internal)

          case v: RegisterListener =>
            active(v :: listeners, cachedConfigs)

          case RemoveListener(listenerId) =>
            active(listeners.filterNot(_.listenerId == listenerId), cachedConfigs)
        }
      }

      def receiveInternalCommand(
          listeners: List[RegisterListener],
          cachedConfigs: Map[String, ConfigContent],
          internal: InternalCommand): Behavior[Command] = {
        internal match {
          case InternalGetResponse(chg, dataIds, replyTo) =>
            chg match {
              case rsp @ Replicator.GetSuccess(`namespaceKey`) =>
                val value = rsp.get(namespaceKey)
                replyTo ! dataIds.flatMap(value.get)
              case _ =>
                replyTo ! Nil
            }
            Behaviors.same

          case InternalAllResponse(chg, replyTo) =>
            chg match {
              case rsp @ Replicator.GetSuccess(`namespaceKey`) =>
                replyTo ! rsp.get(namespaceKey).entries.valuesIterator.toList
              case _ =>
                replyTo ! Nil
            }
            Behaviors.same

          case InternalUpdateResponse(chg, replyTo) =>
            chg match {
              case Replicator.UpdateSuccess(`namespaceKey`) => replyTo ! ModifyReply.ok
              case Replicator.UpdateTimeout(`namespaceKey`) =>
                context.log.warn(s"update ddata timeout $namespaceKey")
                replyTo ! ModifyReply.ok
              case Replicator.ModifyFailure(`namespaceKey`, errorMessage, e) =>
                context.log.error(s"update ddata failure: $errorMessage", e)
                replyTo ! ModifyReply(IntStatus.INTERNAL_ERROR, errorMessage)
              case _ => replyTo ! ModifyReply(IntStatus.INTERNAL_ERROR, "Error")
            }
            Behaviors.same

          case InternalRemoveResponse(chg, replyTo) =>
            chg match {
              case Replicator.UpdateSuccess(`namespaceKey`) => replyTo ! ModifyReply.ok
              case other =>
                context.log.error(s"InternalRemoveResponse($chg, $replyTo) error: $other")
                replyTo ! ModifyReply.error("Error")
            }
            Behaviors.same

          case InternalSubscribeResponse(chg) =>
            context.log.debug(s"subscribe response: $chg, $listeners")
            chg match {
              case v @ Replicator.Changed(`namespaceKey`) =>
                val entries = v.get(namespaceKey).entries
                // added or updated
                entries.foreach {
                  case (_, cc) =>
                    val maybeOld = cachedConfigs.get(cc.namespace)
                    val isChange = maybeOld.exists(_.content != cc.content)
                    val changeType = if (isChange) 2 else if (maybeOld.isEmpty) 1 else 0
                    if (changeType != 0) {
                      val modified = ConfigChanged(
                        cc.namespace,
                        "DEFAULT",
                        cc.namespace,
                        cc.content,
                        oldContent = cachedConfigs.get(cc.namespace).map(_.content),
                        changeType = changeType)
                      notifyListeners(listeners, cc, modified)
                    }
                }

                // deleted
                for {
                  deletedKey <- cachedConfigs.keySet.diff(entries.keySet)
                  cc <- cachedConfigs.get(deletedKey)
                } {
                  val deleted =
                    ConfigChanged(cc.namespace, "DEFAULT", cc.namespace, oldContent = Some(cc.content), changeType = 3)
                  notifyListeners(listeners, cc, deleted)
                }

                active(listeners, entries)

              case Replicator.Deleted(`namespaceKey`) =>
                active(listeners, Map())

              case other => // do nothing
                println(s"other subscribe response: $other")
                Behaviors.same
            }
        }
      }

      def notifyListeners(listeners: List[RegisterListener], cc: ConfigContent, changed: ConfigChanged): Unit = {
        for (listener <- listeners if listener.dataId == cc.namespace) {
          listener.queue
            .offer(changed)
            .foreach {
              case QueueClosed => context.self ! RemoveListener(listener.listenerId)
              case _           => // do nothing
            }(context.executionContext)
        }
      }

      active(Nil, Map())
    }
  }

}
