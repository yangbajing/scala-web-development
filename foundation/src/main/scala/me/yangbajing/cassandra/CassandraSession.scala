package me.yangbajing.cassandra

import java.util

import com.datastax.driver.core.{Cluster, PreparedStatement, Session}
import com.typesafe.scalalogging.StrictLogging

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-24.
 */
private class CqlCache(session: Session) extends StrictLogging {
  private val map = new util.HashMap[String, PreparedStatement]()

  def putIfAbsent(cql: String): PreparedStatement = synchronized {
    if (map.containsKey(cql)) {
      map.get(cql)
    } else {
      logger.debug("new cql: {}", cql)
      val pstmt = session.prepare(cql)
      map.put(cql, pstmt)
      pstmt
    }
  }

}

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-22.
 */
abstract class StandaloneCassandraSession(val cluster: Cluster) {

  /**
   * 获得 Cassandra 连接 Session
   */
  implicit final lazy val session: Session = cluster.connect()

  private lazy val cqlCache = new CqlCache(session)

  /**
   * 生成预编译 CQL 语句
   *
   * @param cql Cassandra CQL 语句
   * @return 若存在就直接返回，不存在则生成返回并缓存
   */
  def prepare(cql: String): PreparedStatement = cqlCache.putIfAbsent(cql)

  def close(): Unit =
    if (cluster != null) {
      cluster.close()
    }

}

class CassandraSession(override val cluster: Cluster) extends StandaloneCassandraSession(cluster)
