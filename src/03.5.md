# 高级服务端 API

除了低级服务端 API，Akka HTTP 还提供了非常灵活的 **Routing DSL** 来优雅的定义 RESTful 风格的 Web 服务。

Akka HTTP 高级 API 提供了如下功能特性：

- 基于高级 API 的 HTTP Server 错误处理
- 服务端 HTTPS 支持
- Routing DSL：将各种元素（Directives 指令）进行组合，以简洁、易读的方式使用 DSL 来表达服务
- Routes：“路由”是 Akka HTTP 提供的 Routing DSL 的核心概念，使用 DSL 构建的所有结构，不管是单条还是很多，都是如下类型的实例：`type Route = RequestContext => Future[RouteResult]`
- Directives：“指令”是用来创建复杂路由结构的小积木。Akka HTTP 已经预定义了大量的 directives，你也可以很方便的建立自己的指令。
- Rejections：在多个路由的构建中，`~` 操作符允许第1条路由被拒绝后继续第2条路由，直到被处理为止。这是，若没有任何路由被匹配，则 "rejections" 可以提供友好的错误处理。
- Exception 处理：当路由中有异常被抛出时，使用 `ExceptionHandler` 可以优雅的进行错误处理或恢复。
- Case Class 提取：可以将请求参数提取成一个 `case class`，如：`parameters('red.as[Int], 'green.as[Int], 'blue.as[Int]).as(Color) { color =>`
- Source Streaming：与 Akka Stream 的结合
- Route TestKit：方便对路由和各 Akka HTTP 功能进行测试
