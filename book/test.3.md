# 端到端测试Route

## Akka HTTP

这里我们将使用 Akka HTTP来开发一个很简单的业务应用：组织管理。它只有一个数据模型，**Org**。组织支持树型结构，所有每个 org 里面都可以有一个可选 `parent` 属性来指向父组织，若没有则代表此 org 是个一级组织。Org 的数据模型如下：

```scala
case class Org(
  id Int,  // PK
  code Option[String], // 组织编码，可选值。Unique index
  name String,
  contact: String,
  parent Option[String], // 父组织
  parents List[String], // 父组织全路径
  status: Int,
  createdAt OffsetDateTime,
  updatedAt Option[OffsetDateTime]
)
```

业务流程上，对一个 Org 模型的操作我们设计如下的简单流程：

```
OrgRoute -> OrgService -> OrgRepository
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

定义一个 Akka HTTP Routing DSL 的测试类，需要混入 `ScalatestRouteTest` 特质，它提供了对 Route DSL 的一系列测试辅助函数。

## OrgRouteTest

现在，我们从 **OrgRouteTest** 开始，通过 <strong style="color:red">红</strong>-<strong style="color:green">绿</strong>-<strong style="color:red">红</strong>-<strong style="color:green">绿</strong>这样的测试循环来验证并一步一步实现对 Org 的各项接口功能。

首先，让我们来看看这个 OrgRouteTest 类：

```scala
class OrgRouteTest extends WordSpec with MustMatchers with OptionValues with ScalatestRouteTest {

}
```

