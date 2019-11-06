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

///*
// * Copyright 2019 helloscala.com
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package fusion.discovery.client
//
//import akka.actor.CoordinatedShutdown
//import akka.actor.typed.ActorSystem
//import akka.actor.typed.scaladsl.adapter._
//import akka.grpc.GrpcClientSettings
//import akka.http.scaladsl.model.Uri.Authority
//import akka.stream.Materializer
//import akka.stream.scaladsl.Source
//import fusion.discovery.grpc.NamingServiceClient
//import fusion.discovery.model._
//
//import scala.concurrent.Future
//import scala.concurrent.duration._
//
//class NamingClient(system: ActorSystem[_]) {
//  import system.executionContext
//  implicit private val classicSystem = system.toClassic
//  implicit private val mat = Materializer(system)
//  private var clients = Map[Authority, NamingServiceClient]()
//
//  val client = NamingServiceClient(GrpcClientSettings.connectToServiceAt("127.0.0.0", 8000))
//  init()
//
//  def makeClient(settings: GrpcClientSettings) = {
//    clients
//  }
//
//  /**
//   * 查询服务状态
//   */
//  def serverStatus(in: ServerStatusQuery): Future[ServerStatusBO] = client.serverStatus(in)
//
//  /**
//   * 添加实例
//   */
//  def registerInstance(in: InstanceRegister): Future[InstanceRegistered] = client.registerInstance(in)
//
//  /**
//   * 修改实例
//   */
//  def modifyInstance(in: InstanceModify): Future[InstanceModified] = client.modifyInstance(in)
//
//  /**
//   * 删除实例
//   */
//  def removeInstance(in: InstanceRemove): Future[InstanceRemoved] = client.removeInstance(in)
//
//  /**
//   * 查询实例
//   */
//  def queryInstance(in: InstanceQuery): Future[InstanceQueried] = client.queryInstance(in)
//
//  private def init(): Unit = {
//    val (cancellable, source) = Source.tick(5.seconds, 5.seconds, InstanceHeartbeat()).preMaterialize()
//    client.heartbeat(source)
//    CoordinatedShutdown(classicSystem).addTask(CoordinatedShutdown.PhaseServiceStop, "NamingClient") { () =>
//      if (!cancellable.isCancelled) {
//        cancellable.cancel()
//      }
//      client.closed()
//    }
//  }
//
//}
