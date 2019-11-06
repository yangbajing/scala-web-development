package scalaweb.respository

import java.util.Properties

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.tminglei.slickpg._
import com.typesafe.config.Config
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import helloscala.common.json.Jackson
import helloscala.common.util.Configuration
import slick.basic.Capability
import slick.jdbc.GetResult
import slick.jdbc.JdbcCapabilities
import slick.util.AsyncExecutor

trait SlickProfile
    extends ExPostgresProfile
    with PgDate2Support
    with PgHStoreSupport
    with PgArraySupport
    with PgJsonSupport {
  override val api: MyAPI.type = MyAPI

  val plainApi = new Date2DateTimePlainImplicits with SimpleHStorePlainImplicits with SimpleJsonPlainImplicits {
    import com.github.tminglei.slickpg.utils.PlainSQLUtils._

    ///
    implicit val getObjectNode: GetResult[ObjectNode] = mkGetResult { pr =>
      val js = pr.nextJson()
      Jackson.defaultObjectMapper.readTree(js.value).asInstanceOf[ObjectNode]
    }

    implicit val getObjectNodeOption: GetResult[Option[ObjectNode]] =
      mkGetResult(_.nextJsonOption().map(js => Jackson.defaultObjectMapper.readTree(js.value).asInstanceOf[ObjectNode]))

    implicit val setObjectNode =
      mkSetParameter[ObjectNode](pgjson, jstr => Jackson.defaultObjectMapper.writeValueAsString(jstr))

    implicit val setObjectNodeOption =
      mkOptionSetParameter[ObjectNode](pgjson, jstr => Jackson.defaultObjectMapper.writeValueAsString(jstr))

  }

  override def pgjson: String = "jsonb"

  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  object MyAPI extends super.API with DateTimeImplicits with HStoreImplicits with ArrayImplicits with JsonImplicits {

    implicit val objectNodeColumnType: BaseColumnType[ObjectNode] =
      MappedColumnType.base[ObjectNode, JsonString]({ node =>
        JsonString(node.toString)
      }, { str =>
        Jackson.defaultObjectMapper.readValue(str.value, classOf[ObjectNode])
      })

    type FilterCriteriaType = Option[Rep[Option[Boolean]]]

    def dynamicFilter(list: Seq[FilterCriteriaType]): Rep[Option[Boolean]] =
      list
        .collect({ case Some(criteria) => criteria })
        .reduceLeftOption(_ && _)
        .getOrElse(Some(true): Rep[Option[Boolean]])

    def dynamicFilter(item: Option[Rep[Boolean]], list: Option[Rep[Boolean]]*): Rep[Boolean] =
      (item +: list).collect({ case Some(criteria) => criteria }).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])

    def dynamicFilterOr(list: Seq[FilterCriteriaType]): Rep[Option[Boolean]] =
      list
        .collect({ case Some(criteria) => criteria })
        .reduceLeftOption(_ || _)
        .getOrElse(Some(true): Rep[Option[Boolean]])

  }

}

object SlickProfile extends SlickProfile {

  def createDatabase(configuration: Configuration): backend.DatabaseDef = {
    val ds = createHikariDataSource(configuration)
    val poolName = configuration.getOrElse[String]("poolName", "default")
    val numThreads = configuration.getOrElse[Int]("numThreads", 20)
    val maximumPoolSize = configuration.getOrElse[Int]("maximumPoolSize", numThreads)
    val registerMbeans = configuration.getOrElse[Boolean]("registerMbeans", false)
    val executor = AsyncExecutor(
      poolName,
      numThreads,
      numThreads,
      configuration.getOrElse[Int]("queueSize", 1000),
      maximumPoolSize,
      registerMbeans = registerMbeans)
    api.Database.forDataSource(ds, Some(maximumPoolSize), executor)
  }

  def createHikariDataSource(data: (String, String), datas: (String, String)*): HikariDataSource = {
    val props = new Properties()
    props.put(data._1, data._2)
    for ((key, value) <- datas) {
      props.put(key, value)
    }
    createHikariDataSource(props)
  }

  def createHikariDataSource(data: Map[String, String]): HikariDataSource = {
    val props = new Properties()
    for ((key, value) <- data) {
      props.put(key, value)
    }
    createHikariDataSource(props)
  }

  private val REMOVED_KEYS = List(
    "useTransaction",
    "ignoreWarnings",
    "allowPrintLog",
    "maxConnections",
    "numThreads",
    "registerMbeans",
    "queueSize")

  @inline def createHikariDataSource(config: Configuration): HikariDataSource =
    createHikariDataSource(config.getProperties(null))

  @inline def createHikariDataSource(config: Config): HikariDataSource =
    createHikariDataSource(Configuration(config))

  @inline def createHikariDataSource(props: Properties): HikariDataSource =
    createHikariDataSource(new HikariConfig(REMOVED_KEYS.foldLeft(props) { (props, key) =>
      props.remove(key); props
    }))

  def createHikariDataSource(config: HikariConfig): HikariDataSource = new HikariDataSource(config)

}
