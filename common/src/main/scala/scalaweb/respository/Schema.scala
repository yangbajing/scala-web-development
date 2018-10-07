package scalaweb.respository

import java.time.OffsetDateTime

import com.fasterxml.jackson.databind.node.ObjectNode
import com.typesafe.config.{Config, ConfigFactory}
import helloscala.common.util.Configuration
import scalaweb.model.Org
import scalaweb.respository.SlickProfile.api._
import slick.basic.DatabasePublisher
import slick.dbio.{DBIOAction, Streaming}

import scala.concurrent.{ExecutionContext, Future}

object Schema {
  def apply(config: Config): Schema = new Schema(Configuration(config))
  def apply(config: Configuration): Schema = new Schema(config)
  def apply(path: String = "scalaweb.persistence.datasource"): Schema = apply(ConfigFactory.load().getConfig(path))
}

class Schema private (conf: Configuration) {
  val profile = SlickProfile

  val db: Database = SlickProfile.createDatabase(conf)

  implicit def executionContext: ExecutionContext = db.ioExecutionContext

  final def run[R](a: DBIOAction[R, NoStream, Nothing]): Future[R] = db.run(a)

  final def runTransaction[R, E <: Effect.Transactional](a: DBIOAction[R, NoStream, E]): Future[R] =
    db.run(a.transactionally)

  final def stream[T](a: DBIOAction[_, Streaming[T], Nothing]): DatabasePublisher[T] = db.stream(a)

  final def streamTransaction[T, E <: Effect.Transactional](a: DBIOAction[_, Streaming[T], E]): DatabasePublisher[T] =
    db.stream(a.transactionally)

  def tOrg = TableQuery[TableOrg]
}

class TableOrg(tag: Tag) extends Table[Org](tag, "t_org") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def code = column[Option[String]]("code")

  def name = column[String]("name")

  def contact = column[ObjectNode]("contact", O.SqlType("text"))

  def parent = column[Option[Int]]("parent")

  def parents = column[List[Int]]("parents")

  def status = column[Int]("status", O.Default(1))

  def createdAt = column[OffsetDateTime]("created_at")

  def updatedAt = column[Option[OffsetDateTime]]("updated_at")

  def * = (id, code, name, contact, parent, parents, status, createdAt, updatedAt).mapTo[Org]
}
