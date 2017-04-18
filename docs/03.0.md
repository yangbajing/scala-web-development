# Akka HTTP 基础

Akka HTTP 是一个基于 `akka-actor` 和 `akka-stream` 的，实现了完整 HTTP 协议的包含 Server/Client 的工具包。它提供了提供和连接 HTTP
服务的通用工具集。它不是一个 Web 框架，因为它不关系于浏览器的交互及服务端页面渲染等（当然，你若需要也可以很方便的集成类似功能。或者直接使用
构建于 Akka HTTP 之上的 [Playframework](https://playframework.com/) ）。

Akka HTTP 是专门为 "Not a framework" 设计的，当你的核心不是与浏览器进行交互、服务端页面渲染……而是提供一些专门的或复杂的服务业务。通过 
Rest/HTTP 接口来发布和连接到应用生态中，这时候一个 Web 框架可能显得比较笨重。这时，Akka HTTP 提供给你一个轻量、简洁、高效地……工具，你不再
需要关心 Javascript/CSS 发布、压缩等，只需要全力专注于服务（API）发布上。

本章将带领读者进入 Akka HTTP 的世界，我们将介绍Akka Http的常用功能模块及使用方式，读完本章你就可以开始使用 Akka HTTP 构建自己的基于 HTTP 
的微服务。Akka Http提供了一套强大、易用、易扩展的route dsl来构建路由。Akka Http Client因还不支持超时功能，现在不建议在产品中使用。

**Akka HTTP官方提供了如下模块：**

- **akka-http-xml**：XML解析库，实现了 Marshalling/Unmarshalling
- **akka-http-jackson**：Jackson JSON解析库，实现了 Marshalling/Unmarshalling
- **akka-http-spary-json**：JSON库，实现了 Marshalling/Unmarshalling
- **akka-http-testkit**：测试工具
- **akka-http**：高级API，包括 Marshall/Unmarshall、Directives等
- **akka-http-core**：低级API，核心功能
- **akka-parsing**：数据包解析等功能

*本章代码见：[codes/akka-http-foundation](https://github.com/yangbajing/scala-web-development/tree/master/codes/akka-http-foundation)*
