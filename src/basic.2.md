# 使用 Akka Http 搭建一个简单的 Web 服务

使用 Akka HTTP 需要对 `akka-http` 的依赖，请把如下配置写入你的 Sbt 工程配置文件中：

```
"com.typesafe.akka" %% "akka-http" % "10.0.5" 
```

## 第一个服务

从官方提供的 `HttpApp` 特质开始，它提供了快捷的方式来启动一个Akka HTTP Server。

```scala
class WebServer extends HttpApp {
  def route: Route =
    path("hello") {
      get {
        import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
        complete(<h1>Say hello to akka-http</h1>)
      }
    }
}
```

`path("hello")`定义了一个HTTP访问路由，`get`代表这个路由提供了**GET**请示，而`complete`涵数允许我们提供响应结果来完成这个路由定义，这里我们返回了一段文本。Akka Http的路由看起来向**声明式**的，以一直新颖而又直观的方式来定义HTTP服务。

用户第一次接触这种涵数套涵数（又像树型结构）的代码方式可能不大习惯，其实我们可以换种方式来实现这段代码：

```scala
  def traditionRoute: Route = {
    val respResult = complete("result") // 响应结果
    val hPath = path("hello") // 绑定的HTTP访问路径
    hPath(get(result)) 
  }
```

`路径(Http方法(结果))`，我们用Java式的风格来实现同样的功能。这样是不是更符合你对代码的预期？

让我们来启动服务：

```scala
object Boot {
  def main(args: Array[String]): Unit = {
    val server = new WebServer
    server.startServer("0.0.0.0", 8888)
  }
}
```

通过**curl**命令来测试下我们的第一个Akka HTTP服务（-i选项可以打印HTTP响应头）：

```
curl -i http://localhost:8888/hello
HTTP/1.1 200 OK
Server: akka-http/10.0.11
Date: Sat, 24 Feb 2018 17:05:12 GMT
Content-Type: text/html; charset=UTF-8
Content-Length: 31

<h1>Say hello to akka-http</h1>
```

## Route

Akka HTTP 提供了一个灵活的**DSL**，它有很多可组合的元素（Directive 指令）以简洁、易读的方式来构建服务。
让我们来看下面这个示例：

```scala
  path("book") {
    get {
      parameters('name.as[Option[String]], 'isbn.as[Option[String]], 'author.as[Option[String]]) {
        (maybeName, maybeIsbn, maybeAuthor) =>
          complete(s"name: $maybeName, isbn: $maybeIsbn, author: $maybeAuthor")
      }
    }
  }
```

对于上面这个定义，类似的 **Play** 路由定义如：

```
GET  /book controller.Page.book(name: Option[String], isbn: Option[String], author: Option[String)
```

我们可以看到，对一个API路由的定义拆成了几个函数嵌套的形式。`path`指定访问路径，`get`决定这个API提供HTTP GET服务，`parameters`可以抽取请求参数，而`complete`将一个字符串返回给前端。

## JSON

现在大部分的服务都提供JSON格式的数据，Akka HTTP提供了 Mashaller/Unmashaller机制，用户可以基于此灵活的定制自己的序列化/反序列化方式。这里我们使用 Jackson 来解析/处理 JSON。

首选，我们实现自定义的 Mashaller/Unmashaller：

```scala
trait JacksonSupport {

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset) => data.decodeString(charset.nioCharset.name)
      }

  // HTTP entity => `A`
  implicit def unmarshaller[A](
                                implicit ct: ClassTag[A],
                                objectMapper: ObjectMapper = Jackson.defaultObjectMapper
                              ): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(
      data => objectMapper.readValue(data, ct.runtimeClass).asInstanceOf[A]
    )

  // `A` => HTTP entity
  implicit def marshaller[A](
                              implicit objectMapper: ObjectMapper = Jackson.defaultObjectMapper
                            ): ToEntityMarshaller[A] = {
    JacksonHelper.marshaller[A](objectMapper)
  }

}
```

实现自定义的 Marshaller/Unmarshaller 后，我们就可以在 Akka HTTP 中提供 Json 支持了。

```scala
package akkahttp.foundation.route

import akka.http.scaladsl.server.Route
import akkahttp.foundation.data.domain.PageInput
import akkahttp.json.JacksonSupport._
import akkahttp.server.BaseRoute

class PageRoute extends BaseRoute {

  def route: Route =
    path("page") {
      post {
        entity(as[PageInput]) { pageInput =>
          complete(pageInput)
        }
      }
    }

}
```

Akka HTTP使用了Scala的隐式转换特性来自定义数据序列化，这是一个非侵入式的设计，用户可以在每个模块选择自己的数据序列化方式。

## Route类型

**Route** 是 Akka HTTP 路由 DSL 里的核心概念，用它构建的所有结构，不管是一条线还是很多条线组成，它们都会是这个类型的实例。

```scala
type Route = RequestContext => Future[RouteResult]
```

**组合路由**

Akka HTTP 提供3个基本的操作来让我们创建更复杂的路由链：

1. 路由转换：它代理一个“内部”的路由，并在这个过程中改变一些请求传入的属性，然后传出响应或两者。
2. 路由过滤：只允许满足给定条件的请求被传递，并拒绝所有其它访问请求。
3. 路由链：如果第一个请求被拒绝，将尝试第二个路由。使用 `~` 操作符连接多个路由定义。

Akka HTTP 实现了很多默认的指令 `akka.http.scaladsl.server.Directives._` ，你也可以很轻松地创建自己的指令。指令提供了强大和灵活的方式来构建 Akka HTTP。

**路由树**

当通过嵌套和操作符组合指令和自定义路径时，将构建一个路由结构并形成一颗树。当一个 HTTP 请求进来，它将从根进行这颗树，并以深度优先的方式流过所有分支，直到某个节点完成或全部被拒绝为止。

```scala
val route =
  a {
    b {
      c {
        ... // route 1
      } ~
      d {
        ... // route 2
      } ~
      ... // route 3
    } ~
    e {
      ... // route 4
    }
  }
```

这里由5个指令构建了一个路由树：

1. 当 a, b, c都通过，才到到达路由 1
2. 当 a 和 b 通过，但 c 被拒绝且 d 通过，将到达路由 2
3. 当 a 和 b 通过，但 c 和 d 被拒绝，路由 3 被到达

若路由 3 前面的请求都被拒绝，则它将“捕获”所有请求。这个机制使复杂的过滤逻辑可以很容易的实现。把简单和最具体的放在顶端，一般和普通的放到最后。
