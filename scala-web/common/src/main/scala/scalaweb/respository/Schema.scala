package scalaweb.respository

import java.time.OffsetDateTime

import scalaweb.model.Org
import scalaweb.respository.SlickProfile.api._

object Schema {
  def apply() = new Schema()
}

class Schema {
  val profile = SlickProfile

  val db = Database.forConfig("sample.datasource")

  def tOrg = TableQuery[TableOrg]
}

class TableOrg(tag: Tag) extends Table[Org](tag, "t_org") {
  def id = column[Int]("id", O.PrimaryKey)

  def code = column[Option[String]]("code")

  def name = column[String]("name")

  def contact = column[String]("contact", O.SqlType("text"))

  def parent = column[Option[Int]]("parent")

  def parents = column[List[Int]]("parents")

  def status = column[Int]("status", O.Default(1))

  def createdAt = column[OffsetDateTime]("created_at")

  def updatedAt = column[Option[OffsetDateTime]]("update_at")

  def * = (id, code, name, contact, parent, parents, status, createdAt, updatedAt).mapTo[Org]
}
