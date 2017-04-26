package me.yangbajing.redis

import me.yangbajing.MeSpec

class RedisSessionTest extends MeSpec with RedisSpec {

  "RedisComponentTest" should {
    "KV set" in {
      redisSession.withClient(cli => cli.set("id", "yangbajing")) mustBe true
    }

    "KV get" in {
      redisSession.withClient(cli => cli.get("id")) mustBe Some("yangbajing")
    }
  }

}
