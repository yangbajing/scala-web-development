# 访问 Redis

## 安装

```
wget -c http://download.redis.io/releases/redis-3.2.8.tar.gz
cd redis-3.2.8
make
# 请设置自己想要的 PREFIX 目录
make PREFIX=/home/local/redis install
cd /home/local/redis/
./bin/redis-server
```

当看到：`23894:M 24 Apr 17:22:13.628 * The server is now ready to accept connections on port 6379` 类似输出则代码 Redis 
服务已正常启动。

## 添加库依赖

使用 [https://github.com/debasishg/scala-redis](https://github.com/debasishg/scala-redis) 库来做为客户端访问 Redis。

```scala
libraryDependencies += "net.debasishg" %% "redisclient" % "3.4"
```

惯例，这里也使用 scalatest 来演示 Redis 的基本使用。

```scala
class RedisSessionTest extends MeSpec with RedisSpec {

  "RedisComponentTest" should {
    "KV set" in {
      redisSession.withClient(cli => cli.set("id", "yangbajing")) shouldBe true
    }

    "KV get" in {
      redisSession.withClient(cli => cli.get("id")) shouldBe Some("yangbajing")
    }
  }

}
```

在本书的后面，我们将触及到更多的 Redis 应用。比如将用户会话信息放到 Redis 中。
