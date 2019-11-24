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

package fusion.discovery.server.config.data

import akka.cluster.ddata.Key
import akka.cluster.ddata.ORMap
import akka.cluster.ddata.ReplicatedData
import fusion.json.jackson.CborSerializable

case class NamespaceKey(namespace: String)
    extends Key[ORMap[String, ConfigContent]](namespace)
    with CborSerializable

case class ConfigContent(
    namespace: String,
    dataId: String,
    version: Int,
    content: String)
    extends ReplicatedData
    with CborSerializable {
  type T = ConfigContent

  override def merge(that: ConfigContent): ConfigContent = {
    if (this == that) this
    else if (namespace == that.namespace && dataId == that.dataId && version < that.version)
      that
    else this
  }
}
