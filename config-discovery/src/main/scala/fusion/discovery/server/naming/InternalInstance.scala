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

import com.typesafe.scalalogging.StrictLogging
import fusion.discovery.model.Instance
import fusion.discovery.model.InstanceHeartbeat
import fusion.discovery.model.InstanceModify
import fusion.discovery.model.InstanceQuery
import fusion.discovery.server.naming.Namings.NamingServiceKey

final private[server] class InternalInstance(private val underlying: Instance)
    extends Ordered[InternalInstance]
    with Equals {
  @transient val instanceId: String = underlying.instanceId
  @transient var lastTickTimestamp: Long = System.currentTimeMillis()

  def healthy: Boolean = (System.currentTimeMillis() - lastTickTimestamp) < Namings.UNHEALTHY_CHECK_THRESHOLD_MILLIS

  def refresh(): InternalInstance = {
    lastTickTimestamp = System.currentTimeMillis()
    this
  }

  def withInstance(in: Instance): InternalInstance = new InternalInstance(in)

  def toInstance: Instance = underlying.copy(healthy = healthy)

  override def compare(that: InternalInstance): Int = {
    if (that.underlying.weight > underlying.weight) 1
    else if (that.underlying.weight < underlying.weight) -1
    else that.underlying.instanceId.compare(underlying.instanceId)
  }

  override def canEqual(that: Any): Boolean = {
    this == that || (that match {
      case other: InternalInstance => other.underlying.instanceId == underlying.instanceId
      case _                       => false
    })
  }

  override def equals(obj: Any): Boolean = canEqual(obj)
}

final private[server] class InternalService(namingServiceKey: NamingServiceKey) extends StrictLogging {
  private var curHealthyIdx = 0
  private var instances = Vector[InternalInstance]()
  private var instIds = Map[String, Int]() // instance id, insts index

  def queryInstance(in: InstanceQuery): Vector[Instance] = {
    logger.debug(s"queryInstance($in); curHealthyIdx: $curHealthyIdx; instIds: $instIds; $instances")
    val selects =
      if (in.allHealthy) allHealthy()
      else if (in.oneHealthy) oneHealthy()
      else allInstance()
    selects.map(_.toInstance)
  }

  def addInstance(inst: Instance): InternalService = {
    val items = instIds.get(inst.instanceId) match {
      case Some(idx) => instances.updated(idx, new InternalInstance(inst))
      case None      => new InternalInstance(inst) +: instances
    }
    saveInstances(items)
    logger.debug(s"addInstance($inst) after; curHealthyIdx: $curHealthyIdx; instIds: $instIds; $instances")
    this
  }

  def modifyInstance(in: InstanceModify): Option[Instance] = {
    val instId = Namings.makeInstanceId(in.ip, in.port, in.serviceName)
    instIds.get(instId).map { idx =>
      val internal = instances(idx)
      val older = internal.toInstance
      val newest = older.copy(
        weight = if (in.weight > 0.0) in.weight else older.weight,
        healthy = in.healthy,
        enabled = in.enabled,
        ephemeral = in.ephemeral,
        metadata = in.metadata)
      saveInstances(instances.updated(idx, internal.withInstance(newest)))
      newest
    }
  }

  def removeInstance(instId: String): Boolean = {
    if (instIds.contains(instId)) {
      saveInstances(instances.filterNot(_.instanceId == instId))
      true
    } else {
      false
    }
  }

  def allInstance(): Vector[InternalInstance] = instances

  def allHealthy(): Vector[InternalInstance] = instances.filter(_.healthy)

  def oneHealthy(): Vector[InternalInstance] = {
    val healths = allHealthy()
    if (healths.isEmpty) {
      curHealthyIdx = 0
      healths
    } else if (curHealthyIdx < healths.size) {
      val ret = healths(curHealthyIdx)
      curHealthyIdx += 1
      Vector(ret)
    } else {
      curHealthyIdx = 1
      Vector(healths.head)
    }
  }

  def processHeartbeat(in: InstanceHeartbeat): InternalService = {
    val instId = Namings.makeInstanceId(in.ip, in.port, in.serviceName)
    instIds.get(instId) match {
      case Some(idx) =>
        instances(idx).refresh()
      case None =>
        val inst = Instance(instId, in.namespace, in.groupName, in.serviceName, in.ip, in.port, enabled = true)
        saveInstances(new InternalInstance(inst) +: instances)
    }
    this
  }

  def refreshHealthy(): InternalService = {
    instances.foreach(_.refresh())
    this
  }

  private def saveInstances(items: Vector[InternalInstance]): Unit = {
    this.instances = items.sortWith(_ > _)
    instIds = this.instances.view.map(_.instanceId).zipWithIndex.toMap
  }

}
