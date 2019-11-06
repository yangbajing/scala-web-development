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

package fusion.discovery.server

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import fusion.http.server.AbstractRoute

class Routes(system: ActorSystem[_]) extends AbstractRoute {
  private val grpcHandler = DiscoveryServer(system).grpcHandler

  override def route: Route =
    pathPrefix("fusion" / "discovery" / "v1") {
      namingRoute ~
      configRoute
    } ~
    extractRequest { request =>
      complete(grpcHandler(request))
    }

  def namingRoute: Route = pathPrefix("naming") {
    completeNotImplemented
  }

  def configRoute: Route = pathPrefix("config") {
    completeNotImplemented
  }
}
