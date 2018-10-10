# 路由DSL

Akka HTTP提供了高级的抽象来定义服务端路由：Route，以简洁和可读的方式将服务行为表示为可组合的元素（称为 Directive (指令)）。指令被组装成起来形成所谓的路由结构，该路由结构的最顶层可用于创建提供给`Http.handleFlow`处理的流（`Flow[HttpRequest, HttpResponse, NotUsed]`，由`Route.handleFlow`显示调用生成，也可由`RouteResult.route2HandlerFlow`来隐式转换）。

之前在 @ref:[\[使用 Akka Http 搭建一个简单的 Web 服务\]](../../basic/basic.2.md) 已经见过了一个例子。本章将深入的讲解 Routing DSL，看完本章后你当可在业务代码中灵活的运用 Routing DSL 。

_**Route** 是Akka HTTP提供的 Routing DSL 的核心概念。使用 DSL 构建的所有路由结构（无论单个还是多个），最终都会将类型`RequestContext`转换为`Future[RouteResult]`。_

@@toc { depth=2 }

@@@ index

- [route](route.md)
- [directive](directive.md)
- [custom-directive](custom-directive.md)

@@@
