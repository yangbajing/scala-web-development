package me.yangbajing.redis

import com.redis.RedisClient
import com.redis.RedisClientPool

/**
 * Redis访问组件
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-24.
 */
class RedisSession(val pool: RedisClientPool) {
  def withClient[T](body: RedisClient => T): T = pool.withClient(body)

  def close(): Unit = pool.close

  override def toString: String = pool.host + ":" + String.valueOf(pool.port)
}

object RedisSession {
  def apply(host: String, port: Int = 6379): RedisSession =
    new RedisSession(new RedisClientPool(host, port))
}
