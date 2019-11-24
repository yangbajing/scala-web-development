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

import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import akka.util.Timeout
import fusion.discovery.grpc.ConfigService
import fusion.discovery.model._
import fusion.discovery.server.config.data.ConfigContent
import helloscala.common.IntStatus

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration._

class ConfigServiceImpl(
    configManager: ActorRef[ConfigManager.Command],
    system: ActorSystem[_])
    extends ConfigService {
  import system.executionContext
  implicit private val mat = Materializer(system)
  implicit private val timeout = Timeout(5.seconds)
  implicit private val scheduler = system.scheduler

  override def serverStatus(in: ServerStatusQuery): Future[ServerStatusBO] =
    Future.successful(ServerStatusBO(IntStatus.OK))

  override def queryConfig(in: ConfigQuery): Future[ConfigReply] = {
    configManager
      .ask[immutable.Seq[ConfigContent]](replyTo =>
        ConfigManager.GetContent(in.namespace, in.dataIds, replyTo))
      .map { contents =>
        val queried =
          ConfigQueried(contents.map(c =>
            ConfigItem(c.namespace, "DEFAULT", c.dataId, c.content)))
        ConfigReply(IntStatus.OK, ConfigReply.Data.Queried(queried))
      }
  }

  override def publishConfig(in: ConfigPublish): Future[ConfigReply] = {
    configManager
      .ask[Configs.ModifyReply](replyTo =>
        ConfigManager.UpdateContent(in.namespace, in.dataId, in.content, replyTo))
      .map(reply => ConfigReply(reply.status))
  }

  override def removeConfig(in: ConfigRemove): Future[ConfigReply] = {
    configManager
      .ask[Configs.ModifyReply](replyTo =>
        ConfigManager.RemoveContent(in.namespace, in.dataId, replyTo))
      .map(reply => ConfigReply(reply.status))
  }

  override def listenerConfig(
      in: ConfigChangeListen): Source[ConfigChanged, NotUsed] = {
    val (queue, source) =
      Source.queue[ConfigChanged](8, OverflowStrategy.dropHead).preMaterialize()
    configManager ! ConfigManager.RegisterChangeListener(
      UUID.randomUUID(),
      in,
      queue)
    source
  }
}
