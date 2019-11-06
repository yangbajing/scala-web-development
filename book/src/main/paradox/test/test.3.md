# 端到端测试Route

## Akka HTTP

这里我们将使用 Akka HTTP来开发一个很简单的业务应用：组织管理。它只有一个数据模型，**Org**。组织支持树型结构，所有每个 org 里面都可以有一个可选 `parent` 属性来指向父组织，若没有则代表此 org 是个一级组织。[Org](https://github.com/yangbajing/scala-web-development/blob/master/scala-web/common/src/main/scala/scalaweb/model/Org.scala) 的数据模型如下：

```scala
case class Org(
  id Int,  // PK
  code Option[String], // 组织编码，可选值。Unique index
  name String,
  contact: ObjectNode, // Json类型，使用Jackson
  parent Option[String], // 父组织
  parents List[String], // 父组织全路径
  status: Int,
  createdAt OffsetDateTime,
  updatedAt Option[OffsetDateTime]
)
```

业务流程上，对一个 Org 模型的操作我们设计如下的简单流程：

```
OrgRoute -> OrgService -> OrgRepo
```

`OrgRoute` 是一个使用 **Akka HTTP Routing DSL** 来定义实现的路由（从MVC架构术语来说，就是控制器（Controller））。从这个简单的示例来说，它拥有如下接口：

- **createRoute**：创建 Org
- **getRoute**：根据id或code获取 Org
- **pageRoute**：分页查询
- **updateRoute**：更新 Org
- **removeRoute**：根据id删除 Org

现在，我们已经设计好了我们需要的5个接口（名字），接下来需要定义具体的接口和实现。这里，我们先从测试开始。

## akka-http-testkit

Akka HTTP 提供了一个测试套件来简化对 Akka HTTP 和 Akka HTTP Routing DSL的测试，我们需要在 sbt 配置里加上对应的库依赖：

```
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "2.5.14" % Test
```

定义一个 Akka HTTP Routing DSL 的测试类，需要混入 `ScalatestRouteTest` 特质，它提供了对 Route DSL 的一系列测试辅助函数来支持Scalatest。

## OrgRouteTest

现在，我们从 **OrgRouteTest** 开始，通过 <strong style="color:red">红</strong>-<strong style="color:green">绿</strong>-<strong style="color:red">红</strong>-<strong style="color:green">绿</strong>这样的测试循环来验证并一步一步实现对 Org 的各项接口功能。

首先，让我们来看看这个 [OrgRouteTest](https://github.com/yangbajing/scala-web-development/blob/master/scala-web/test/src/test/scala/scalaweb/test/route/OrgRouteTest.scala) 类：

```scala
class OrgRouteTest
    extends WordSpec
    with BeforeAndAfterAll
    with ScalatestRouteTest
    with Matchers
    with OptionValues
    with ScalaFutures {

  private val schema = new Schema()
  private var orgIds: Set[Int] = Set()
  private val orgService = new OrgService(schema)
  private val route: Route = new OrgRoute(orgService).route

  "OrgRoute" should {
    import helloscala.http.JacksonSupport._

    var org: Org = null

    "create" in {
      val req = OrgCreateReq(Some("000001"), "测试组织", None, None)
      Post("/org/item", req) ~> route ~> check {
        status shouldBe StatusCodes.Created
        org = responseAs[Org]
        orgIds += org.id
        org.id should be > 0
        org.parent shouldBe None
        org.updatedAt shouldBe None
      }
    }

    "get" in {
      pending
    }

    "pageRoute" in {
      pending
    }

    "updateRoute" in {
      pending
    }

    "remoteRoute" in {
      pending
    }
  }

  private def cleanup(): Unit = try {
    orgService.removeByIds(orgIds).futureValue
  } catch {
    case NonFatal(e) => e.printStackTrace()
  }

  override def afterAll() {
    cleanup()
    schema.db.close()
    super.afterAll()
  }

}
```

完整代码请见：[https://github.com/yangbajing/scala-web-development/blob/master/scala-web/test/src/test/scala/scalaweb/test/route/OrgRouteTest.scala](https://github.com/yangbajing/scala-web-development/blob/master/scala-web/test/src/test/scala/scalaweb/test/route/OrgRouteTest.scala)。

这里的**OrgRouteTest**测试类继续了多个接口：

- **WordSpec**：使用了**Word**风格的测试，必需要混入这样的一个测试规范接口。类似的有：**FutureSpec**、**FlatSpec**等
- **BeforeAndAfterAll**：提供了所有测试用例执行前或完成后的钩子函数
- **ScalatestRouteTest**：Akka HTTP Routing 测试的辅助函数
- **Matchers**：用户友好的断言DSL
- **OptionValues**：Option类型的辅助函数
- **ScalaFutures**：Future类型的辅助函数

这里可以看到定义了5个测试样例，其中 **create** 测试样例已经实现，其它4个暂未实现，使用 `pending` 函数占位。

在测试开始，定义了3个变量：

1. `schema`：数据库连接管理
2. `orgIds`：测试生成的组织ID列表，待测试完成后可用来进行数据清除
3. `route`：要测试的路由

#### routing test dsl

在 **create** 测试用例中，使用 `~>` 函数连接了 `Post`（请求）、`route`（路由）、`check`（检测函数）三个部分。`Post`定义了我们要发起的测试请求，通过 `~>` 符号（函数）连接（发送）到`route`（路由），然后再用 `~>` 将响应连接到 `check` 函数来做检测（断言）。`Post`实际是调用了 [RequestBuilder](https://github.com/akka/akka-http/blob/master/akka-http/src/main/scala/akka/http/scaladsl/client/RequestBuilding.scala)，`RequestBuilder`有多个重载函数，这里使用了需要一个`ToEntityMarshaller[T]`隐式转换的函数，它可以将我们提交的`OrgCreateReq`请求（case class）转换成合适的HTTP数据并设置匹配的`Content-Type`请求头：
```scala
    def apply[T](uri: String, content: T)(implicit m: ToEntityMarshaller[T], ec: ExecutionContext): HttpRequest =
      apply(uri, Some(content))
```

我们使用`import helloscala.http.JacksonSupport._`来导入Akka HTTP的JSON支持来将请求转换成`application/json`类型的HTTP请求数据发送到`route`。*Akka HTTP JSON支持见：[JSON](data.1.md)*

**check**

`route`响应的结果将通过 check 函数来进行测试断言，通过`ScalatestRouteTest`提供了多个辅助函数来完成测试，而对Akka HTTP routing的各类处理细节被隐藏在了`check`函数的调用内部。常用的辅助函数有：

- `status`：获取响应的HTTP状态码
- `response`：获取响应数据
- `responseAs[T: FromResponseUnmarshaller]`：将响应数据（body）转换成T类型
- `entityAs[T: FromEntityUnmarshaller]`：类似responseAs，但是将`HttpEntity`转换成T类型
- `contentType`：获取响应的Conent-Type
- `mediaType：获取响应的MediaType
- `headers`：获取响应的所有header头
- `header[T >: Null <: HttpHeader: ClassTag]`：查找指定类型的响应header头，返回结果为`Option[T]`

#### run test

在sbt console中执行命令运行测试：` test/testOnly scalaweb.test.route.OrgRouteTest`，结果如下：

```
01:04:39.075 INFO com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Starting...
01:04:39.238 INFO com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Start completed.
01:04:40.439 DEBUG slick.jdbc.JdbcBackend.statement - Preparing insert statement (returning: id,code,name,contact,parent,parents,status,created_at,updated_at): insert into "t_org" ("code","name","contact","parent","parents","status","created_at","updated_at")  values (?,?,?,?,?,?,?,?)
01:04:40.468 DEBUG slick.jdbc.JdbcBackend.benchmark - Execution of prepared update took 4ms
01:04:41.410 DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: delete from "t_org" where "t_org"."id" in (5)
01:04:41.411 DEBUG slick.jdbc.JdbcBackend.benchmark - Execution of prepared update took 499µs
01:04:41.443 INFO com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Shutdown initiated...
01:04:41.448 INFO com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Shutdown completed.
[info] OrgRouteTest:
[info] OrgRoute
[info] - should create
[info] - should get (pending)
[info] - should pageRoute (pending)
[info] - should updateRoute (pending)
[info] - should remoteRoute (pending)
[info] ScalaTest
[info] Run completed in 3 seconds, 382 milliseconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 4
[info] All tests passed.
[info] Passed: Total 1, Failed 0, Errors 0, Passed 1, Pending 4
[success] Total time: 5 s, completed 2018-8-22 1:04:41
```

可以看到，这里执行通过了一个测试用例`should create`，有4个测试用例为**pending**（代表还未实现）。

## 总结

使用 akka-http-testkit 可以在不启动Server的情况下对定义的route进行测试，可以显著的提高测试效率。


