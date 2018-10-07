package me.yangbajing.cassandra

import com.datastax.driver.core.{Cluster, DataType}
import com.datastax.driver.extras.codecs.jdk8.{InstantCodec, LocalDateCodec, LocalTimeCodec, ZonedDateTimeCodec}
import me.yangbajing.cassandra.codec.LocalDateTimeCodec

case class CassandraConf(
    nodes: Seq[String],
    clusterName: String,
    username: Option[String],
    password: Option[String],
    keyspace: Option[String])

object CassandraHelper {

  def getCluster(nodes: Seq[String], clusterName: String = "Test Cluster"): Cluster =
    getCluster(CassandraConf(nodes, clusterName, None, None, None))

  /**
   * 获得 Cassandra 连接 Cluster
   */
  def getCluster(c: CassandraConf): Cluster = {
    val cluster = Cluster.builder.addContactPoints(c.nodes: _*).withClusterName(c.clusterName).build
    val tupleType = cluster.getMetadata.newTupleType(DataType.timestamp, DataType.varchar)
    cluster.getConfiguration.getCodecRegistry
      .register(LocalDateTimeCodec.instance)
      .register(InstantCodec.instance)
      .register(LocalDateCodec.instance)
      .register(LocalTimeCodec.instance)
      .register(new ZonedDateTimeCodec(tupleType))
    cluster
  }

}
