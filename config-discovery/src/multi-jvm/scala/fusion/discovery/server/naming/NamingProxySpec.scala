package fusion.discovery.server.naming

import akka.actor.typed.scaladsl.adapter._
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.cluster.typed.Subscribe
import akka.cluster.typed.Unsubscribe
import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory
import helloscala.common.IntStatus

import scala.concurrent.duration._

object NamingProxySpecConfig extends MultiNodeConfig {
  // register the named roles (nodes) of the test
  // note that this is not the same thing as cluster node roles
  val first = role("first")
  val second = role("second")
  val third = role("thrid")

  // this configuration will be used for all nodes
  // note that no fixed host names and ports are used
  commonConfig(ConfigFactory.parseString("""
    akka.actor.provider = cluster
    """).withFallback(ConfigFactory.load()))

  nodeConfig(first)(ConfigFactory.parseString("""{akka.remote.artery.canonical.port = 12551}"""))
  nodeConfig(second)(ConfigFactory.parseString("""{akka.remote.artery.canonical.port = 12552}"""))
  nodeConfig(third)(ConfigFactory.parseString("""{akka.remote.artery.canonical.port = 12553}"""))

}

// need one concrete test class per node
class NamingProxySpecMultiJvmNode1 extends NamingProxySpec
class NamingProxySpecMultiJvmNode2 extends NamingProxySpec
class NamingProxySpecMultiJvmNode3 extends NamingProxySpec

class NamingProxySpec extends FusionMultiNodeSpec(NamingProxySpecConfig) {
  import NamingProxySpecConfig._

  private lazy val namingProxy = {
    val shardRegion =
      ClusterSharding(typedSystem).init(Entity(Namings.TypeKey)(entityContext => Namings(entityContext.entityId)))
    FusionCore(typedSystem).spawnActorSync(NamingProxy(shardRegion), NamingProxy.NAME, 3.seconds)
  }
  private lazy val namingService = new NamingServiceImpl(namingProxy, typedSystem)

  "NamingProxy" should {
    val namespace = "namespace"
    val groupName = "DEFAULT"
    val dataId = "service-naming"
    val serviceName = "service-naming"
    val content = """{data-server {name = "data-service"}}"""

    "illustrate how to startup cluster" in within(10.seconds) {
      Cluster(typedSystem).subscriptions ! Subscribe(testActor.toTyped[MemberEvent], classOf[MemberUp])
      //expectMsgClass(classOf[MemberUp])

      val firstAddress = node(first).address
      val allAddress = Set(firstAddress, node(second).address, node(third).address)

      Cluster(typedSystem).manager ! Join(firstAddress)

      receiveN(3).collect { case MemberUp(m) => m.address }.toSet should be(allAddress)

      Cluster(typedSystem).subscriptions ! Unsubscribe(testActor.toTyped[MemberEvent])

      enterBarrier("all-up")
    }

    "server status" in {
      val serverStatus = namingService.serverStatus(ServerStatusQuery()).futureValue
      serverStatus.status shouldBe IntStatus.OK
      enterBarrier("server-status")
    }

    "register & query instance" in {
      runOn(first) {
        enterBarrier("register-instanced")
      }

      runOn(second) {
        val ip = typedSystem.settings.config.getString("fusion.http.default.server.host")
        val port = typedSystem.settings.config.getInt("fusion.http.default.server.port")
        val in = InstanceRegister(namespace, serviceName, groupName, ip = ip, port = port, enabled = true)
        val registered = namingService.registerInstance(in).futureValue
        registered.status shouldBe IntStatus.OK
        enterBarrier("register-instanced")
      }

      runOn(third) {
        enterBarrier("register-instanced")
        val in = InstanceQuery(namespace, serviceName, groupName, allHealthy = true)
        val reply = namingService.queryInstance(in).futureValue
        reply.status shouldBe IntStatus.OK
        val queried = reply.data.asInstanceOf[InstanceReply.Data.Queried].value
        queried.instances should not be empty
        println(s"find healthy instance ${queried.instances.size}")
        queried.instances.foreach(println)
      }

      enterBarrier("register-query-finished")
    }

//    "remove instance" in {
//      runOn(first) {
//        val in = InstanceRemove(namespace,  serviceName)
//        val removed = namingService.removeInstance(in).futureValue
//        removed.status shouldBe IntStatus.OK
//        enterBarrier("remove-instance")
//      }
//      runOn(second) {
//        enterBarrier("remove-instance")
//        val in = InstanceQuery(namespace, serviceName, groupName, allHealthy = true)
//        val queried = namingService.queryInstance(in).futureValue
//        queried.status shouldBe IntStatus.NOT_FOUND
//      }
//      runOn(third) {
//        enterBarrier("remove-instance")
//      }
//
//      enterBarrier("remove-instance-finished")
//    }
  }

}
