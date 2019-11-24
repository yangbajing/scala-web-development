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

package fusion.discovery.server.naming

import java.util.concurrent.TimeoutException

import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.stream.scaladsl.Source
import akka.util.Timeout
import fusion.discovery.grpc.NamingService
import fusion.discovery.model._
import helloscala.common.IntStatus
import helloscala.common.util.StringUtils

import scala.concurrent.Future
import scala.concurrent.duration._

class NamingServiceImpl(
    namingProxy: ActorRef[Namings.Command],
    system: ActorSystem[_])
    extends NamingService {
  import system.executionContext
  implicit private val timeout = Timeout(5.seconds)
  implicit private val scheduler = system.scheduler

  /**
   * 查询服务状态
   */
  override def serverStatus(in: ServerStatusQuery): Future[ServerStatusBO] =
    Future.successful(ServerStatusBO(IntStatus.OK))

  /**
   * 添加实例
   */
  override def registerInstance(in: InstanceRegister): Future[InstanceReply] = {
    namingProxy
      .ask[InstanceReply](replyTo => Namings.RegisterInstance(in, replyTo))
      .recover {
        case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
      }
  }

  /**
   * 修改实例
   */
  override def modifyInstance(in: InstanceModify): Future[InstanceReply] = {
    namingProxy
      .ask[InstanceReply](replyTo => Namings.ModifyInstance(in, replyTo))
      .recover {
        case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
      }
  }

  /**
   * 删除实例
   */
  override def removeInstance(in: InstanceRemove): Future[InstanceReply] = {
    namingProxy
      .ask[InstanceReply](replyTo => Namings.RemoveInstance(in, replyTo))
      .recover {
        case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
      }
  }

  /**
   * 查询实例
   */
  override def queryInstance(in: InstanceQuery): Future[InstanceReply] = {
    namingProxy
      .ask[InstanceReply](replyTo => Namings.QueryInstance(in, replyTo))
      .recover {
        case _: TimeoutException => InstanceReply(IntStatus.GATEWAY_TIMEOUT)
      }
  }

  override def heartbeat(
      in: Source[InstanceHeartbeat, NotUsed]): Source[ServerStatusBO, NotUsed] = {
    in.map { cmd =>
      if (checkHeartbeat(cmd)) {
        namingProxy ! Namings.Heartbeat(cmd)
        ServerStatusBO(IntStatus.OK)
      } else {
        ServerStatusBO(IntStatus.BAD_REQUEST)
      }
    }
  }

  @inline private def checkHeartbeat(v: InstanceHeartbeat): Boolean = {
    StringUtils.isNoneBlank(v.namespace) && StringUtils.isNoneBlank(v.ip) && v.port > 0 &&
    StringUtils.isNoneBlank(v.serviceName)
  }
}
