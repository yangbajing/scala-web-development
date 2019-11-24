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

package fusion.discovery

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.remote.testkit.MultiNodeConfig
import akka.remote.testkit.MultiNodeSpec
import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Millis
import org.scalatest.time.Span

import scala.language.implicitConversions

trait STMultiNodeSpec
    extends MultiNodeSpecCallbacks
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures {
  self: MultiNodeSpec =>

  override def initialParticipants: Int = roles.size

  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5000, Millis)), scaled(Span(50, Millis)))

  implicit def typedSystem: ActorSystem[_] = system.toTyped

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = {
    multiNodeSpecAfterAll()
  }

  // Might not be needed anymore if we find a nice way to tag all logging from a node
  implicit override def convertToWordSpecStringWrapper(
      s: String): WordSpecStringWrapper =
    new WordSpecStringWrapper(s"$s (on node '${self.myself.name}', $getClass)")
}

abstract class FusionMultiNodeSpec(multiNodeConfig: MultiNodeConfig)
    extends MultiNodeSpec(
      multiNodeConfig,
      config => akka.actor.ActorSystem(config.getString("fusion.name"), config))
    with STMultiNodeSpec
