# 使用 Redis/Elasticsearch

## Redis

**安装**

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

**添加库依赖**

使用 [https://github.com/debasishg/scala-redis](https://github.com/debasishg/scala-redis) 库来做为客户端访问 Redis。

```scala
libraryDependencies += "net.debasishg" %% "redisclient" % "3.4"
```

惯例，这里也使用 scalatest 来演示 Redis 的基本使用。

```scala
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
```

在本书的后面，我们将触及到更多的 Redis 应用。比如将用户会话信息放到 Redis 中。

## Elasticsearch

**安装**

在 [https://elastic.co/](https://elastic.co) 官网下载对应操作系统版本。

```
cd elasticsearch-5.3.1
./bin/elasticsearch
```

看到类似输出则代表 Elasticsearch 已正常启动：

```
[2017-04-25T11:58:40,412][INFO ][o.e.h.HttpServer         ] [MTz4O40] publish_address {127.0.0.1:9200}, bound_addresses {[::1]:9200}, {127.0.0.1:9200}
[2017-04-25T11:58:40,413][INFO ][o.e.n.Node               ] [MTz4O40] started
```

可以使用 REST 接口来访问 Elasticsearch：

```
curl -XPUT http://localhost:9200/blog/article/1?pretty -d '{"title": "新版本Elasticsearch发布", "content": "5.3.1今天发布了", "tags": ["announce", "elasticsearch", "发布"] }'
```

返回结果如下，可以看到：`"result": "created"`，这代表这是一个新建操作，当前版本号为 1。

```json
{
  "_index" : "blog",
  "_type" : "article",
  "_id" : "1",
  "_version" : 1,
  "result" : "created",
  "_shards" : {
    "total" : 2,
    "successful" : 1,
    "failed" : 0
  },
  "created" : true
}
```

**添加库依赖**

```scala
libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-tcp" % "5.3.1"
```



