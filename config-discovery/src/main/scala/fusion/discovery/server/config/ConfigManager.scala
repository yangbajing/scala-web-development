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
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.SourceQueueWithComplete
import fusion.discovery.model.ConfigChangeListen
import fusion.discovery.model.ConfigChanged
import fusion.discovery.server.config.data.ConfigContent
import fusion.discovery.server.config.data.NamespaceKey
import fusion.json.jackson.CborSerializable

import scala.collection.immutable

object ConfigManager {
  sealed trait Command extends CborSerializable

  case class GetContent(
      namespace: String,
      dataIds: immutable.Seq[String],
      replyTo: ActorRef[immutable.Seq[ConfigContent]])
      extends Command

  case class UpdateContent(namespace: String, dataId: String, content: String, replyTo: ActorRef[Configs.ModifyReply])
      extends Command
  case class RemoveContent(namespace: String, dataId: String, replyTo: ActorRef[Configs.ModifyReply]) extends Command
  case class AllContent(namespace: String, replyTo: ActorRef[immutable.Seq[ConfigContent]]) extends Command

  case class RegisterChangeListener(
      listenerId: UUID,
      in: ConfigChangeListen,
      queue: SourceQueueWithComplete[ConfigChanged])
      extends Command

  val NAME = "configManager"

  //val ConfigManagerServiceKey = ServiceKey[Command](NAME)

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    //context.system.receptionist ! Receptionist.Register(ConfigManagerServiceKey, context.self)
    active(context, Map())
  }

  private def active(
      context: ActorContext[Command],
      children: Map[String, ActorRef[Configs.Command]]): Behavior[Command] = {
    def activeThan(namespace: String, childFunc: ActorRef[Configs.Command] => Unit): Behavior[Command] = {
      children.get(namespace) match {
        case Some(child) =>
          childFunc(child)
          Behaviors.same
        case None =>
          val child = context.spawn(Configs(NamespaceKey(namespace)), namespace)
          childFunc(child)
          active(context, children.updated(namespace, child))
      }

    }

    Behaviors.receiveMessagePartial {
      case GetContent(namespace, dataIds, replyTo) =>
        activeThan(namespace, _ ! Configs.GetContent(dataIds, replyTo))
      case UpdateContent(namespace, dataId, content, replyTo) =>
        activeThan(namespace, _ ! Configs.UpdateContent(dataId, content, replyTo))
      case RemoveContent(namespace, dataId, replyTo) =>
        activeThan(namespace, _ ! Configs.RemoveContent(dataId, replyTo))
      case AllContent(namespace, replyTo) =>
        activeThan(namespace, _ ! Configs.AllContent(replyTo))
      case RegisterChangeListener(listenerId, in, queue) =>
        activeThan(in.namespace, _ ! Configs.RegisterListener(listenerId, in.dataId, queue))
    }
  }

}
