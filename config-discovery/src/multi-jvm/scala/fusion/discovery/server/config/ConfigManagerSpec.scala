package fusion.discovery.server.config

import java.util.concurrent.TimeUnit

import akka.actor.typed.scaladsl.adapter._
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.cluster.typed.Subscribe
import akka.cluster.typed.Unsubscribe
import akka.remote.testkit.MultiNodeConfig
import akka.stream.Materializer
import akka.testkit.ImplicitSender
import com.typesafe.config.ConfigFactory
import helloscala.common.IntStatus

import scala.concurrent.duration._

object ConfigManagerSpecConfig extends MultiNodeConfig {
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

  nodeConfig(first)(ConfigFactory.parseString("""akka.remote.artery.canonical.port = 12551"""))
  nodeConfig(second)(ConfigFactory.parseString("""akka.remote.artery.canonical.port = 12552"""))
  nodeConfig(third)(ConfigFactory.parseString("""akka.remote.artery.canonical.port = 12553"""))

}

// need one concrete test class per node
class ConfigManagerSpecMultiJvmNode1 extends ConfigManagerSpec
class ConfigManagerSpecMultiJvmNode2 extends ConfigManagerSpec
class ConfigManagerSpecMultiJvmNode3 extends ConfigManagerSpec

abstract class ConfigManagerSpec extends FusionMultiNodeSpec(ConfigManagerSpecConfig) with ImplicitSender {
  import ConfigManagerSpecConfig._

  private lazy val configManager =
    FusionCore(typedSystem).spawnActorSync(ConfigManager(), ConfigManager.NAME, 3.seconds)
  private lazy val configService = new ConfigServiceImpl(configManager, typedSystem)

  "ConfigManager" should {
    val namespace = "namespace"
    val dataId = "service-config-id"
    val groupName = "DEFAULT"
    val content = """{data-server {name = "data-service"}}"""

    "illustrate how to startup cluster" in within(10.seconds) {
      Cluster(typedSystem).subscriptions ! Subscribe(testActor.toTyped[MemberEvent], classOf[MemberUp])
      //expectMsgClass(classOf[MemberUp])

      val firstAddress = node(first).address
      val secondAddress = node(second).address
      val thirdAddress = node(third).address

      Cluster(typedSystem).manager ! Join(firstAddress)

      receiveN(3).collect { case MemberUp(m) => m.address }.toSet should be(
        Set(firstAddress, secondAddress, thirdAddress))

      Cluster(typedSystem).subscriptions ! Unsubscribe(testActor.toTyped[MemberEvent])

      testConductor.enter("all-up")
    }

    "serverStatus" in {
      runOn(first) {
        val serverStatus = configService.serverStatus(ServerStatusQuery()).futureValue
        serverStatus.status shouldBe IntStatus.OK
      }
    }

    "listenerConfig" in within(10.seconds) {
      implicit val mat = Materializer(typedSystem)
      runOn(second) {
        configService
          .listenerConfig(ConfigChangeListen(namespace, groupName, dataId))
          .runForeach(changed => println(s"Receive changed: $changed"))
      }
    }

    "publishConfig" in within(10.seconds) {
      runOn(third) {
        val in = ConfigPublish(namespace, groupName, dataId, content)
        val published = configService.publishConfig(in).futureValue
        published.status should be(IntStatus.OK)
      }
    }

    "queryConfig" in within(10.seconds) {
      runOn(first) {
        val in = ConfigQuery(namespace, List(dataId))
        val reply = configService.queryConfig(in).futureValue
        reply.status should be(IntStatus.OK)
        val queried = reply.data.asInstanceOf[ConfigReply.Data.Queried].value
        println(queried)
        queried.configs.headOption.foreach { config =>
          println(config)
          config.namespace should be(namespace)
          config.dataId should be(dataId)
          config.groupName should be(groupName)
          config.content should be(content)
        }
      }
    }

    "removeConfig" in within(10.seconds) {
      runOn(second) {
        TimeUnit.SECONDS.sleep(1)
        val in = ConfigRemove(namespace, groupName, dataId)
        val removed = configService.removeConfig(in).futureValue
        removed.status should be(IntStatus.OK)
      }
    }
  }

}
