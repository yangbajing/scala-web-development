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
import java.util.concurrent.TimeUnit

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.util.Timeout
import fusion.discovery.model.ConfigPublish
import fusion.discovery.model.ConfigQuery
import fusion.discovery.model.ConfigReply
import helloscala.common.IntStatus
import helloscala.test.FusionTestWordSpec
import org.scalatest.time.Millis
import org.scalatest.time.Seconds
import org.scalatest.time.Span

import scala.concurrent.duration._

class ConfigEntityTest extends ScalaTestWithActorTestKit(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    """) with FusionTestWordSpec {

  implicit override val patience: PatienceConfig =
    PatienceConfig(scaled(Span(10, Seconds)), scaled(Span(15, Millis)))
  implicit override val timeout: Timeout = 5.seconds

  private val sharding = ClusterSharding(system)
  private val configShard = ConfigEntity.initShard(sharding)

  "ConfigEntity" must {
    val namespace = "namespace"
    val dataId = "dataId"
    val groupName = "groupName"
    val content = """{config.example = "Config Example"}"""
    val entityId = s"$namespace&$dataId"
    "publish" in {
      val configEntity: EntityRef[ConfigEntity.Command] =
        sharding.entityRefFor(ConfigEntity.TypeKey, entityId)
      TimeUnit.SECONDS.sleep(5)
      println(s"configEntity: $configEntity")
      val published = configEntity
        .ask[ConfigReply](replyTo =>
          ConfigEntity.PublishContent(ConfigPublish(namespace, dataId, groupName, content), replyTo))
        .futureValue
      println(s"Published: $published")
      TimeUnit.SECONDS.sleep(5)
    }

    "query" in {
      val reply = configShard
        .ask[ConfigReply](replyTo =>
          ShardingEnvelope(entityId, ConfigEntity.QueryContent(ConfigQuery(namespace, List(dataId)), replyTo)))
        .futureValue
      reply.status should be(IntStatus.OK)
      val queried = reply.data.asInstanceOf[ConfigReply.Data.Queried].value
      println(s"query result: $queried")
    }
  }

}
