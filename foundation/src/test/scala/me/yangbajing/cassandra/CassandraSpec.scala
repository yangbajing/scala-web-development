package me.yangbajing.cassandra

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-24.
 */
trait CassandraSpec extends BeforeAndAfterAll {
  this: Suite =>

  var cassandraSession: CassandraSession = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    val cluster = CassandraHelper.getCluster(Seq("localhost"))
    cassandraSession = new CassandraSession(cluster)
  }

  override def afterAll(): Unit = {
    cassandraSession.close()
    //dataSource
    super.afterAll()
  }
}
