# 访问 Cassandra 数据库

## Cassandra 数据模型

**Cassandra 与 传统关系性数据库的区别及特性**

- No joins：没有关联操作，对于在 SQL 中这类操作你需要先获得主表数据后再通过 id 去关联，或者反范式化设计将关联表数据做为扩展内
  容直接存到主表中。
- No referential integrity：没有外键约束，没有参照完整性，当然也没有级联操作。需要在应用中小心注意删除或修改关联数据。
- Denormalization：非范式化，使用 Cassandra 进行数据建模使用范式化设计是不高效的。
- Query-first design：查询先行的设计，使用 Cassandra 建模需要首先、小心设计好查询方式。因为它只有在根据主键搜索时（3.0开始支
  持第二索引，但效率不高）才能高效。
- Designing for optimal storage：在建模设计时就需要考虑存储优化，使用关系性数据库时一般是不用管数据存储的。但使用 Cassandra 
  时就需要提前规划，因为 Cassandra 的数据是默认分区的，而分区的形式对性能有着很大的影响。
- Sorting is a design decision：设计时需要决定排序方式。在建表时就决定了排序方式，并且只有 Cluster Key 支持排序。

Cassandra 的数据模型可以简单类比为：`Map<PartitionKey, SortedMap<ClusterKey, Value>>`，也可以简化成：KKV。

![Cassandra Table](imgs/CassandraTable.png)

Cassandra 有如下主要数据结构：

- **column**：列，类似于 name/value 对。
- **row**：包含多个列的容器，并使用主键引用。
- **table**：包含多个行的容器。
- **keyspace**：包含多个表的容器。
- **cluster**：集群，包含多个 keyspace 的容器，使用一个或多个节点。

## 安装、配置 Cassandra

**下载、安装**

```
wget -c http://archive.apache.org/dist/cassandra/3.9/apache-cassandra-3.9-bin.tar.gz
tar zxf apache-cassandra-3.9-bin.tar.gz
cd apache-cassandra-3.9
./bin/cassandra
```

**初始化数据库、表**

```
./bin/cqlsh
cqlsh> CREATE KEYSPACE hldev WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};
cqlsh> use hldev;
cqlsh:hldev> CREATE TABLE user(
         ...   id TIMEUUID,
         ...   email TEXT,
         ...   name TEXT,
         ...   created_at TIMESTAMP,
         ...   salt blob,
         ...   salt_password blob,
         ...   PRIMARY KEY (id, email, name)
         ... );
```

这里创建了一个 keyspace，一张表，对于我们的示例足够了。

## Alpakka Cassandra Connector

这里我们介绍怎样使用 [Alpakka](http://developer.lightbend.com/docs/alpakka/0.7/index.html) 的 
[Cassandra Connector](http://developer.lightbend.com/docs/alpakka/0.7/cassandra.html) 来访问 Cassandra。
Alpakka Cassandra是基于 Akka Stream 的一个Connector，它提供了使用 Akka Stream 的编程模型来访问 Cassandra 数据库的功能。

**添加库依赖**

```
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "0.7"
```

**访问Cassandra**

继续能过单元测试来看看怎样使用 Alpakka Cassandra Connector 访问 Cassandra 数据库的。

```scala
sbt
> testOnly akkahttp.foundation.data.repository.UserRepositoryTest
```

测试结果如下：

![testOnly akkahttp.foundation.data.repository.UserRepositoryTest](imgs/testOnly-UserRepositoryTest.png)

这里，我们把注意力关注在 `UserRepository.scala` 上，`UserRepositoryTest` 单元测试和上一节访问 PostgreSQL 库的测试无本质区
别，但数据库访问代码区别可就大了。

首先，我们使用了 `Alpakka` 里提供的 Akka Stream for Cassandra Connector 来访问，并没有直接使用官方提供的 `cassandra-driver-core` 。

```scala
class UserRepository(cassandraSession: StandaloneCassandraSession
                    )(
                      implicit val materializer: Materializer, ec: ExecutionContext
                    ) {

  import cassandraSession.session  // (1)

  def deleteById(userId: UUID): Future[Done] = {
    val stmt = cassandraSession.prepare("delete from hldev.user where id = ?").bind(userId)
    CassandraSource(stmt).runWith(Sink.ignore)  // (2)
  }

  def insert(user: User, password: SaltPassword): Future[Done] = {
    val stmt = cassandraSession
      .prepare("insert into hldev.user(id, email, name, created_at, salt, salt_password) values(?, ?, ?, ?, ?, ?)")
      .bind(
        user.id,
        user.email,
        user.email.split('@')(0),
        user.createdAt,
        ByteBuffer.wrap(password.salt),
        ByteBuffer.wrap(password.saltPwd))
    CassandraSource(stmt).runWith(Sink.ignore)
  }

  def login(email: String, password: String): Future[Option[(User, Array[Byte], Array[Byte])]] = {
    val stmt = cassandraSession
      .prepare("select * from hldev.user where email = ? ALLOW FILTERING")
      .bind(email)
    CassandraSource(stmt)
      .runWith(Sink.headOption) // (3)
      .map(maybeRow => // (4)
        maybeRow.map(row =>
          (UserRepository.mapToUser(row),
            Utils.byteBufferToArray(row.getBytes("salt")),
            Utils.byteBufferToArray(row.getBytes("salt_password")))
        ))
  }

  def findById(userId: UUID): Future[Option[User]] = {
    val stmt = cassandraSession
      .prepare("select * from hldev.user where id = ?")
      .bind(userId)
    CassandraSource(stmt)
      .runWith(Sink.headOption)
      .map(maybeRow => maybeRow.map(row => UserRepository.mapToUser(row)))
  }

  def existsByEmail(email: String): Future[Boolean] = {
    CassandraSource(cassandraSession.prepare("select count(1) as COUNT from hldev.user where email = ? ALLOW FILTERING").bind(email))
      .runWith(Sink.head)
      .map(row => row.getLong("COUNT") == 1)
  }
}
```

- (1)：首先需要从 `cassandraSession` 中导入 `com.datastax.driver.core.Session` 异常变量，这个是 `CassandraSource` 需要的
- (2)：对于 **DELETE** 语句，Cassandra 驱动并没向SQL一样返回语句影响的行数这样的返回值，这里在 `runWith` 函数里使用 `Sink.ignore` 忽略返回值
- (3)：对于 `login` 这个操作，我们使用 `Sink.headOption` 获取第一个值。若没有找到任务匹配值则返回 `None`
- (4)：转换获取的 `ResultSet` 为 `User` 对象

Alpakka Cassandra Connector 提供了兼容 Akka Stream 的方式来快速访问 Cassandra 数据库，使用响应式的编程。对于 Akka Stream，
可以阅读 [Akka Stream Quicktart](http://doc.akka.io/docs/akka/2.4/scala/stream/stream-quickstart.html) 快速了解其基本原理
和使用方式。本书介绍的 Akka HTTP 是构建在 Akka Stream 之上的，而 Alpakka Cassansdra Connector可以紧密地和Akka HTTP进行结合，
使我们的应用吏响应式、高效。

> 注意：在使用 Cassandra 提供的批量插入功能时，也许需要修改下数据库默认配置。在使用 `BatchStatement` 进行插入操作时会发现，
> 当数据量稍大以后数据库中并没有加入新的数据。这是因为Cassandra默认对批量操作的数据大小限制得比较低。我们将其修改即可。

```bash
# Log WARN on any batch size exceeding this value. 5kb per batch by default.
# Caution should be taken on increasing the size of this threshold as it can lead to node instability.
batch_size_warn_threshold_in_kb: 1000

# Fail any batch exceeding this value. 50kb (10x warn threshold) by default.
batch_size_fail_threshold_in_kb: 2000
```

