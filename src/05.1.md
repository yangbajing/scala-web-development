# 使用 JDBC 访问 PostgreSQL

在 Scala 中，Java 的数据库访问方式都可以使用，比如：JDBC、JPA、MyBatis、Hibernate等。除此之外，也有些专为 Scala 制作的数据库访问库，如：
Slick、ScalikeJDBC、Quill等。但这里，我们介绍在 Scala 中怎样使用 JDBC。

## PostgreSQL

首先，安装 **PostgreSQL** 数据库。

**Linux**

```bash
apt-get install postgresql // Debian/Ubuntu
yum install postgresql // Fedora/CentOS/RHEL
```

**Mac**

```bash
brew install postgresql
```

**Windows**

到 EnterpriseDB 官网下载二进制安装包：`[https://www.enterprisedb.com/downloads/postgres-postgresql-downloads#windows](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads#windows)`

**配置网络**

Linux下，PostgreSQL默认是不允许通过远程网络访问的。我们需要修改下它的默认配置。

- *Ubunux*：`/etc/postgresql/9.6/main` 目录
- *CentOS*：`/var/lib/pgsql/data` 目录

1. 编辑 `postgresql.conf` 文件，去掉 `listen_address` 的注释，并修改为：

```
listen_address = '*'
```

同时也去掉 `password_encryption` 的注释。

2. 编辑 `pg_hba.conf` 文件，添加：`host    all      all    0.0.0.0/0    md` ，设置允许所有网络访问并使用 `md5` 形式的密码登录机制。

> （注意：修改PostgreSQL的配置文件后需要重启数据库以使其生效）

**创建账号及初使化库、表**

*创建账号、数据库*

```
sudo su - postgres
psql
postgres=# create user hldev nosuperuser encrypted password 'hldev';
postgres=# create database hldev owner=hldev template=template0 encoding='UTF-8' lc_collate='zh_CN.UTF-8' lc_ctype='zh_CN.UTF-8';
```

*初始化表、数据*

```sql
CREATE TABLE author(
  id bigserial,
  name VARCHAR(255) NOT NULL,
  age int,
  remark TEXT,
  PRIMARY KEY (id)
);

CREATE TABLE book(
  isbn VARCHAR(64),
  author bigint NOT NULL,
  title VARCHAR(255) NOT NULL,
  description VARCHAR(1024),
  amount DECIMAL NOT NULL,
  PRIMARY KEY (isbn)
);
```

## 访问数据库

### 命令行

```
psql -h localhost -d hldev -U hldev
```

### 代码访问

首先，我们定义两个数据库实体对象：`Author` 和 `Book`：

```scala
case class Author(id: Long,
                  name: String,
                  age: Option[Int],
                  remark: Option[String])
case class Book(isbn: String,
                author: Long,
                title: String,
                amount: BigDecimal,
                description: Option[String])
```

接下来在 `AuthorRepository` 中进行数据库操作，现在先把什么 `Service`，`Controller`等放一边，我们使用 **scalatest** 来测试下我们的数
据库访问代码。

```scala
class AuthorRepositoryTest extends MeSpec with JDBCSpec {

  "AccountRepositoryTest" should {
    val accountRepository = new AuthorRepository(dataSource)

    "create" in {
      val account = Author(0, "羊八井", Some(31), None)
      val result = accountRepository.create(account)
      result.id must be > 0L
    }

    "update" in {
      val author = Author(3, "yangbajing", Some(32), Some("中国重庆江津"))
      val result = accountRepository.update(author)
      result.id mustBe author.id
      result.name mustBe author.name
      result.age mustBe author.age
    }

    "list" in {
      val results = accountRepository.list()
      results must not be empty
      println(s"results size: ${results.size}")
    }

  }
  
}
```

先不管 `MeSpec` 和 `JDBCSpec` 两个 trait，你可以先试试这个测试（前题是你已经设置好数据库环境）。

```scala
> testOnly akkahttp.foundation.data.repository.AuthorRepositoryTest
```

![testOnly akkahttp.foundation.data.repository.AuthorRepositoryTest](imgs/testOnly-AuthorRepositoryTest.png)

若一切正常，你会看到测试正确通过。`AuthorRepositoryTert` 脚本中一共执行了3个测试，其中 `should list` 测试中打印了一个测试输出，从数据库中
获取到8条记录。下面，让我们来看看 `AuthorRepository` ，我们怎么在 Scala 中使用 JDBC 来访问数据库的。

```scala
class AuthorRepository(dataSource: DataSource) {
  val jdbcTemplate = JdbcTemplate(dataSource) // ①

  def update(author: Author): Author = {
    require(author.id > 0L, "id 必有大于 0") // ②

    val (names, args) = AuthorRepository.generateArgs(author)
    val updateSet = JdbcTemplate.sqlUpdateSets(names)
    val sql = s"UPDATE author SET $updateSet WHERE id = ? RETURNING *" // ③
    args.append(author.id.asInstanceOf[Object])

    val (results, _) = jdbcTemplate.queryMany(sql, args)
    if (results.isEmpty) {
      throw new SQLException(s"账号：${author.id} 不存在")
    } else {
      AuthorRepository.generateResult(results.head) // ④
    }
  }

  def create(author: Author): Author = {
    require(author.id <= 1L, "id 不能存在")
      
    val (names, args) = AuthorRepository.generateArgs(author)
    val sql =
      s"""INSERT INTO author(${JdbcTemplate.sqlNames(names)})
         |  VALUES(${JdbcTemplate.sqlArgs(args)}) RETURNING id""".stripMargin // ⑤

    val (results, labels) = jdbcTemplate.queryMany(sql, args)

    val id = results.head.apply(labels.head.label).asInstanceOf[Long]
    author.copy(id = id)
  }

  def list(): Vector[Author] = {
    val (results, _) = jdbcTemplate.queryMany("SELECT id, name, age, remark FROM author ORDER BY id DESC")
    results.map(AuthorRepository.generateResult)
  }

}
```

- ① 这里我们没有使用 Ioc 等方式来管理组件件的依赖，我们在类的构造代码区里实例华一个 `jdbcTemplate`。
- ② 在 `update` 方法的开头，我们效验 Author `id` 是否有效。
- ③ 主意这里的 SQL 语句，在普通的 `update` 语句之上添加了 **RETURNING *** 语句。这里 **PostgreSQL** 数据库的一个扩展特性，它将返回语句影
响记录的所有字段。可以通过这个特性很方便的在更新语句之后获取到最新的数据。
- ④ `generateResult` 函数，把返回的键值对形式的数据转换映射成一个 `Author` 对象
- ⑤ 这里使用 `RETURNING id` 返回数据库序列自动生成的 Author `id`

在 `AuthorRepository` 的伴生对象里，有两个简单的帮助函数，`generateResult` 和 `generateArgs` ，分别将通过JDBC的ResultSet获取的记录映射
成一个 `Author` 对象和将 `author` 对象转换成可用于 `INSERT`、`UPDATE`语句字段序列部分及获取有效参数的一个序列。这段话说起来有点拗口，直接来
看看 `generateArgs` 的代码：

```scala
  def generateArgs(author: Author) = {
    val names = mutable.Buffer.empty[String]
    val args = mutable.Buffer.empty[Object]

    names.append("name")
    args.append(author.name)

    author.age.foreach { value =>
      names.append("age")
      args.append(Integer.valueOf(value))
    }

    author.remark.foreach { remark =>
      names.append("remark")
      args.append(remark)
    }
    (names, args)
  }
```

我们有一个 Author 对象的实例：Author(0, "羊八井", Some(31), None)。在调用 `generateArgs` 函数后，将获得以下转出结果：

```js
(["name", "age"], ["羊八井", 32])
```

我们获得了一个 Tuple（元组），第一个值是字段名的序列，第二个值是数据（参数）序列。有了这两个序列后，再使用 `JdbcTemplate.sqlNames` 和
 `JdbcTemplate.sqlArgs` 来简化我们 SQL 语句的拼装。
 
```scala
  // [name, age] => "name" = ?, "age" = ?
  def sqlUpdateSets(names: Seq[String]): String = names.map(name => s""""$name" = ?""").mkString(", ")

  // [name, age] => "name", "age"
  def sqlNames(names: Seq[String]): String = names.mkString("\"", "\", \"", "\"")

  // [value1, value2] => ?, ?
  def sqlArgs(args: Seq[_]): String = args.map(_ => "?").mkString(", ")
```

经过这些简单的函数步骤，我们就可以很方便的拼接出想要的SQL语句了。

```scala
// INSERT INTO author(name, age) VALUES(?, ?) RETURNING id
val sql = """INSERT INTO author(${JdbcTemplate.sqlNames(names)}) VALUES(${JdbcTemplate.sqlArgs(args)}) RETURNING id"""
```

JDBC是Java里标准的SQL访问方式，Scala 作为一门 JVM 语言是天然支持的。而 PostgreSQL 是开源界里优秀的关系型数据库，也是功能做为丰富的。这里我们
知道了在 Scala 里面怎样使用 SQL 来访问关系型数据库，下一节将介绍怎样访问 Cassandra 这一强大的 NoSQL 数据库。
