# 访问 Elasticsearch

## 安装

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

## 添加库依赖

```scala
libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-tcp" % "5.3.1"
```



