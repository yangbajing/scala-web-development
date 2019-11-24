package akkahttp.foundation.data.repository

import java.sql.SQLException
import javax.sql.DataSource

import akkahttp.foundation.data.entity.Author
import me.yangbajing.jdbc.JdbcTemplate

import scala.collection.mutable

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-19.
 */
// #AuthorRepository
class AuthorRepository(dataSource: DataSource) {
  val jdbcTemplate = JdbcTemplate(dataSource)

  def update(author: Author): Author = {
    require(author.id > 0L, "id 必有大于 0")

    val (names, args) = AuthorRepository.generateArgs(author)
    val updateSet = JdbcTemplate.sqlUpdateSets(names)
    val sql = s"UPDATE author SET $updateSet WHERE id = ? RETURNING *"
    args.append(author.id.asInstanceOf[Object])

    val (results, _) = jdbcTemplate.queryMany(sql, args)
    if (results.isEmpty) {
      throw new SQLException(s"账号：${author.id} 不存在")
    } else {
      AuthorRepository.generateResult(results.head)
    }
  }

  def create(author: Author): Author = {
    require(author.id <= 1L, "id 不能存在")

    val (names, args) = AuthorRepository.generateArgs(author)
    val sql =
      s"""INSERT INTO author(${JdbcTemplate.sqlNames(names)})
         |  VALUES(${JdbcTemplate.sqlArgs(args)}) RETURNING id""".stripMargin

    val (results, labels) = jdbcTemplate.queryMany(sql, args)

    val id = results.head.apply(labels.head.label).asInstanceOf[Long]
    author.copy(id = id)
  }

  def list(): Vector[Author] = {
    val (results, _) =
      jdbcTemplate.queryMany(
        "SELECT id, name, age, remark FROM author ORDER BY id DESC")
    results.map(AuthorRepository.generateResult)
  }
}
// #AuthorRepository

object AuthorRepository {
  // #help-function
  def generateResult(result: Map[String, AnyRef]): Author =
    Author(
      result("id").asInstanceOf[Long],
      result("name").asInstanceOf[String],
      result.get("age").map(_.asInstanceOf[Int]),
      result.get("remark").map(_.asInstanceOf[String]))

  /**
   * 获取有效的数据库查询字段名列表和参数值列表
   *
   * @param author Author实例
   * @return
   */
  def generateArgs(
      author: Author): (mutable.Buffer[String], mutable.Buffer[Object]) = {
    val names = mutable.Buffer.empty[String]
    val args = mutable.Buffer.empty[Object]

    names.append("name")
    args += author.name

    author.age.foreach { value =>
      names.append("age")
      args += Integer.valueOf(value)
    }

    author.remark.foreach { remark =>
      names.append("remark")
      args += remark
    }
    (names, args)
  }
  // #help-function
}
