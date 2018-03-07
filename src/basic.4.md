# Akka HTTP 如何使得 Web 工作

Akka HTTP 有 **Low-Level** 和 **High-Level** 两套服务端 API，这里我们来看看 **Low-Level** API，这会使你对 Akka HTTP 是怎样运行会
有一个深入的认识。

Akka HTTP 提供了一个基于 Reactive-Streams (反映式流)，全异步的 HTTP/1.1 服务端实现，支持以下特性：

- 完整支持 HTTP 持久化连接
- 完整支持 HTTP 流水线
- 完整支持异步 HTTP 流，包括 "chunked" （分块的）编码
- SSL/TLS 加密支持
- WebSocket 支持

Akka HTTP 服务端组件有两层：

1. `akka-http-core`：基本的低级API实现，提供了更偏底层的 HTTP 功能
2. `akka-http`：高级API实现，提供了易用的DSL（Directives）

**low-level** 聚焦于提供实现完整 HTTP/1.1 协议的基本功能：

- 连接管理
- 解析和渲染“消息”及 headers
- 超时管理（请求连接和服务端 accept 连接）
- 响应顺序（透明的流水线 (pipeline) 支持）

所有的非核心功能都放到了 `akka-http` 模块，如：路由、文件处理、压缩等等）。

## 启动和停止

Akka HTTP 提供了 `bind` 这个方法来启动服务，它通过指定 `interface` 和 `port` 来绑定服务，并注册处理进入的 HTTP 连接。

```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

implicit val system = ActorSystem()
implicit val materializer = ActorMaterializer()
implicit val executionContext = system.dispatcher

val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
  Http().bind(interface = "localhost", port = 8080)
val bindingFuture: Future[Http.ServerBinding] =
  serverSource.to(Sink.foreach { connection => // foreach materializes the source
    println("Accepted new connection from " + connection.remoteAddress)
    // ... and then actually handle the connection
  }).run()
```

`Http.ServerBinding` 实例有一个 `unbind()` 方法，通过它可以停止 Akka HTTP 服务。

## 请求、响应生命周期

低级别的 Akka HTTP 服务端 API 在 `akka-http-core` 模块提供了 `HttpRequest` 接受多个或单个连接，并由 `HttpResponse` 生成响应。处理这
些请求/响应的函数调用类型叫做：`Flow[HttpRequest, HttpResponse, _]` ，由它来“转换” **请求** 到 **响应** 。

收到的 HTTP 请求通过调用 `handleWithXXX` 中的一个来处理，主要的方法有：

> - `Flow[HttpRequest, HttpResponse, _]` 由 `handleWith` 方法使用
> - `HttpRequest => HttpResponse` 函数由 `handleWithSyncHandler` 方法使用
> - `HttpRequest => Future[HttpResponse]` 函数由 `handleWithAsyncHandler` 方法使用

一个 `HttpRequest => HttpResponse` 函数的例子如下：

```scala
val requestHandler: HttpRequest => HttpResponse = {
  case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
    HttpResponse(entity = HttpEntity(
      ContentTypes.`text/html(UTF-8)`,
      "<html><body>Hello world!</body></html>"))

  case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
    HttpResponse(entity = "PONG!")

  case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
    sys.error("BOOM!")

  case r: HttpRequest =>
    r.discardEntityBytes() // important to drain incoming HTTP Entity stream
    HttpResponse(404, entity = "Unknown resource!")
}

val bindingFuture: Future[Http.ServerBinding] =
  serverSource.to(Sink.foreach { connection =>
    println("Accepted new connection from " + connection.remoteAddress)

    connection handleWithSyncHandler requestHandler
    // this is equivalent to
    // connection handleWith { Flow[HttpRequest] map requestHandler }
  }).run()
```

在这个例子里，一个请求被 `HttpRequest => HttpResponse` 函数处理，使用 `handleWithSyncHandler` 来绑定了这一系列转换操作（等价于 
Akka Stream 的 `map` 操作）。

## 错误处理

在 Akka HTTP 服务初始化和运行中可能有各种各样的故障或错误情况发生。Akka 默认记录下了所有这些错误，但有时候需要自行处理这些错误，如：记录错误
发生的情况，关闭 **Actor系统**，或通知一些外部的监控端点……

有很多情况会造成在创建和实现一个服务时错误，有各种类型的错误，常见的包括：

- `bind` 到指定 address/port 失败。
- 接受新的 `IncommingConnection` 时失败，如操作系统可打开文件描述符已用完或内存不足。
- 处理一个连接时错误，如传入的请求数据无效。

下面介绍下常见的故障情况及怎样处理这些故障。

**绑定失败 (Bind failures)**

```scala
val serverSource = Http().bind("localhost", 80)

val bindingFuture: Future[ServerBinding] = serverSource
  .to(handleConnections) // Sink[Http.IncomingConnection, _]
  .run()

bindingFuture.onFailure {
  case ex: Exception =>
    log.error(ex, "Failed to bind to {}:{}!", "localhost", 80)
}
```

当 `bind` 到 80 端口时，很有可能程序没有权限（需要 `root` 权限才能绑定到 80 端口），或者端口已被其它程序占用……这时就会发生**绑定故障**。上
面例子里，`bindingFuture` 将立即失败，我们可以监听 `onFaliure` 函数对指定的异常做出处理。

**连接源失败 (Connection Source failures)**

下面的例子，我们通过一个 `failureMonitor` actor来捕获 `IncomingConnection` 错误，由 Actor 来处理这个错误，也许它会决定重启服务或关闭
整个 **ActorSystem**　。

```scala
val failureMonitor: ActorRef = system.actorOf(MyExampleMonitoringActor.props)

val reactToTopLevelFailures = Flow[IncomingConnection]
  .watchTermination()((_, termination) => termination.onFailure {
    case cause => failureMonitor ! cause
  })

serverSource
  .via(reactToTopLevelFailures)
  .to(handleConnections) // Sink[Http.IncomingConnection, _]
  .run()
```

**连接失败 (Connection failures)**
 
故障发生的第3种情况是连接已经建立，但在响应前突然终止，如：client中止了底层的TCP连接。这理此类故障可以使用与前一个相似的方式，但是我们将这个应
用这个连接的处理流程中。

```scala
val reactToConnectionFailure = Flow[HttpRequest]
  .recover[HttpRequest] {
    case ex =>
      // handle the failure somehow
      throw ex
  }

val httpEcho = Flow[HttpRequest]
  .via(reactToConnectionFailure)
  .map { request =>
    // simple streaming (!) "echo" response:
    HttpResponse(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, request.entity.dataBytes))
  }

serverSource
  .runForeach { con =>
    con.handle
```

大部分时间你都不需要深入理解这个故障处理，Akka 详细的记录了这些故障日志，并有一系列默认的方式来处理这些故障。
