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

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import fusion.discovery.model._
import fusion.json.jackson.CborSerializable
import helloscala.common.IntStatus

object ConfigEntity {
  private val RESERVED_VERSIONS_TAKE = 19

  sealed trait Command extends CborSerializable
  case class PublishContent(in: ConfigPublish, replyTo: ActorRef[ConfigReply]) extends Command
  case class QueryContent(in: ConfigQuery, replyTo: ActorRef[ConfigReply]) extends Command

  sealed trait Event extends CborSerializable
  case class Published(in: ConfigPublish) extends Event

  case class Content(content: String, version: Int) extends CborSerializable

  case class State(namespace: String, dataId: String, groupName: String, contents: List[Content])
      extends CborSerializable {
    @transient def headContent: Option[Content] = contents.headOption
    @transient def headContentVersion: Int = if (contents.isEmpty) -1 else contents.head.version
  }

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("ConfigEntity")

  /**
   * entityId: <namespace>|<dataId>|[<groupName>]
   */
  def initShard(sharding: ClusterSharding): ActorRef[ShardingEnvelope[Command]] = {
    sharding.init(Entity(TypeKey) { entityContext =>
      val namespace :: dataId :: others = entityContext.entityId.split('&').toList
      apply(
        entityContext.entityId,
        PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId),
        State(namespace, dataId, others.headOption.getOrElse(""), Nil))
    })
  }

  def apply(entityId: String, persistenceId: PersistenceId, initState: State): Behavior[Command] = Behaviors.setup {
    context =>
      context.log.info(
        s"ConfigEntity start up. entityId: $entityId, persistenceId: $persistenceId, initState: $initState")

      EventSourcedBehavior[Command, Event, State](
        persistenceId,
        initState,
        commandHandler(context),
        eventHandler(context))
  }

  private def commandHandler(context: ActorContext[Command])(state: State, cmd: Command): Effect[Event, State] = {
    context.log.debug(s"commandHandler($state, $cmd)")
    cmd match {
      case PublishContent(in, replyTo) =>
        Effect.persist(Published(in)).thenRun { newState =>
          val result = newState match {
            case State(_, _, _, content :: _) if content.version > state.headContentVersion =>
              ConfigReply(IntStatus.OK)
            case _ =>
              ConfigReply(IntStatus.INTERNAL_ERROR)
          }
          context.log.debug(s"PublishContent thenRun, result: $result, newState: $newState")
          replyTo ! result
        }
      case QueryContent(in, replyTo) =>
        val reply = state.contents.headOption match {
          case Some(content) =>
            val value = ConfigQueried(List(ConfigItem(content = content.content)))
            ConfigReply(IntStatus.OK, ConfigReply.Data.Queried(value))
          case _ =>
            ConfigReply(IntStatus.NOT_FOUND)
        }
        Effect.reply(replyTo)(reply)
    }
  }

  private def eventHandler(context: ActorContext[Command])(state: State, event: Event): State = {
    context.log.debug(s"eventHandler($state, $event)")
    event match {
      case Published(in) =>
        context.log.debug(s"Published receive: $in")
        val version = state.contents.headOption.map(_.version + 1).getOrElse(0)
        val contents = Content(in.content, version) :: state.contents.take(RESERVED_VERSIONS_TAKE)
        state.copy(contents = contents)
    }
  }

}
