package me.yangbajing.jdbc

import org.postgresql.ds.PGSimpleDataSource
import org.scalatest.{BeforeAndAfterAll, Suite}

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-19.
 */
trait JDBCSpec extends BeforeAndAfterAll {
  this: Suite =>

  val dataSource = new PGSimpleDataSource

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    dataSource.setServerName("localhost")
    dataSource.setDatabaseName("hldev")
    dataSource.setUser("hldev")
    dataSource.setPassword("hldev")
  }

  override def afterAll() {
    //dataSource
    super.afterAll()
  }

}
