package me.yangbajing.jdbc

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import javax.sql.DataSource

import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable
import scala.util.control.NonFatal

case class HlJdbcConnection(underlying: Connection, fetchSize: Int = 50)

object HlJdbcConnection {
  final val empty: HlJdbcConnection = null
}

case class HlSqlLabel(label: String, name: String, jdbcType: Int)

/**
 *
 * @param dataSource 数据源
 */
class JdbcTemplate private (val dataSource: DataSource) extends StrictLogging {

  /**
   * 执行insert, update, delete, alert 语句
   *
   * @param sql    SQL语句
   * @param args   占位符参数
   * @param hlConn JDBC连接
   * @return 返回语句成功执行后影响的数据行数
   * @throws SQLException SQL执行或数据库异常
   */
  @throws(classOf[SQLException])
  def executeUpdate(sql: String, args: scala.collection.Seq[Object] = Nil)(
      implicit hlConn: HlJdbcConnection = HlJdbcConnection.empty): Int = {
    val func = (pstmt: PreparedStatement) => pstmt.executeUpdate()

    execute(func, sql, args)
  }

  def querySingle(sql: String, args: scala.collection.Seq[Object] = Nil)(
      implicit igConn: HlJdbcConnection = HlJdbcConnection.empty): Option[(Map[String, Object], Vector[HlSqlLabel])] = {
    val (results, metaDatas) = queryMany(sql, args)
    //    if (results.size > 1) throw new IllegalAccessException(s"$sql 返回结果大于1行")
    results.headOption.map(map => (map, metaDatas))
  }

  def queryMany(sql: String, args: scala.collection.Seq[Object] = Nil)(
      implicit igConn: HlJdbcConnection = HlJdbcConnection.empty): (Vector[Map[String, Object]], Vector[HlSqlLabel]) = {
    val func = (pstmt: PreparedStatement) => {
      val results = mutable.ArrayBuffer.empty[Map[String, Object]]
      val rs = pstmt.executeQuery()
      val metaData = rs.getMetaData

      val range = (1 to metaData.getColumnCount).toVector

      val labels = range.map(column =>
        HlSqlLabel(metaData.getColumnLabel(column), metaData.getColumnName(column), metaData.getColumnType(column)))

      while (rs.next()) {
        val maps = range.map(column => labels(column - 1).label -> rs.getObject(column)).toMap
        results += maps
      }
      (results.toVector, labels)
    }

    execute(func, sql, args)
  }

  def execute[R](resultSetFunc: PreparedStatement => R, sql: String, args: scala.collection.Seq[Object])(
      implicit igConn: HlJdbcConnection = HlJdbcConnection.empty): R =
    _execute { conn =>
      val pstmt = conn.underlying.prepareStatement(sql)
      pstmt.setFetchSize(conn.fetchSize)
      for ((arg, idx) <- args.zipWithIndex) {
        pstmt.setObject(idx + 1, arg)
      }

      logger.debug("[SQL]: {} [ARGS]: {}", sql, args.mkString(", "))

      resultSetFunc(pstmt)
    }

  private def _execute[R](func: HlJdbcConnection => R)(
      implicit igConn: HlJdbcConnection = HlJdbcConnection.empty): R = {
    val conn = if (igConn == HlJdbcConnection.empty) HlJdbcConnection(dataSource.getConnection) else igConn
    try {
      func(conn)
    } finally {
      if (!conn.underlying.isClosed)
        conn.underlying.close()
    }
  }

  def doInTransaction[R](func: HlJdbcConnection => R): R = {
    val conn = HlJdbcConnection(dataSource.getConnection)
    JdbcTemplate.using(conn)(func)
  }

}

object JdbcTemplate {

  Class.forName("org.postgresql.Driver")

  // [name, age] => "name" = ?, "age" = ?
  def sqlUpdateSets(names: Iterable[String]): String = names.map(name => s""""$name" = ?""").mkString(", ")

  // [name, age] => "name", "age"
  def sqlNames(names: Iterable[String]): String = names.mkString("\"", "\", \"", "\"")

  // [value1, value2] => ?, ?
  def sqlArgs(args: Iterable[_]): String = args.map(_ => "?").mkString(", ")

  def apply(dataSource: DataSource): JdbcTemplate = new JdbcTemplate(dataSource)

  def using[R](conn: HlJdbcConnection)(func: HlJdbcConnection => R): R = {
    val originalCommit = conn.underlying.getAutoCommit
    try {
      conn.underlying.setAutoCommit(false)
      val result = func(conn)
      conn.underlying.commit()
      result
    } catch {
      case NonFatal(e) =>
        conn.underlying.rollback()
        throw e
    } finally {
      conn.underlying.setAutoCommit(originalCommit)
      if (!conn.underlying.isClosed)
        conn.underlying.close()
    }
  }

  def using[R](conn: Connection)(func: Connection => R): R = {
    val autoCommit = conn.getAutoCommit
    try {
      conn.setAutoCommit(false)
      val result = func(conn)
      conn.commit()
      result
    } catch {
      case NonFatal(e) =>
        conn.rollback()
        throw e
    } finally {
      if (conn != null) {
        conn.close()
      }
      conn.setAutoCommit(autoCommit)
    }
  }

}
