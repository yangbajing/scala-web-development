package me.yangbajing.redis

import org.scalatest.{BeforeAndAfterAll, Suite}

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-24.
 */
trait RedisSpec extends BeforeAndAfterAll {
  this: Suite =>

  val redisSession = RedisSession("localhost")

  override protected def afterAll(): Unit = {
    redisSession.close()
    super.afterAll()
  }

}
