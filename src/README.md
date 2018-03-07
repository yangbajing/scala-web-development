# Scala Web 开发 -- 使用 Akka HTTP

本文将主要基于 [**Akka HTTP**](http://doc.akka.io/docs/akka-http/current/index.html) 介绍怎样使用 Scala 进行 Web 开发。

**Scala**

Scala是一个面向对象的函数式特性编程语言，它继承了Java的面向对特性，同时又从[`Haskell`](https://www.haskell.org/)等其它语言那里吸收了很多函数式特性并做了增强。

**Akka**

Akka 是一个在 JVM 上构建高并发、分布式、弹性的消息驱动应用程序的工具包。它使创建强大并发且具有分布式特性的应用程序更轻松：

- Simple Concurrency & Distribution（易使用的并发与分布式）：
    异步和分布式设计，具有 Actors、Streams 和 Futures 等高级抽象。

- Resilient By Design（弹性设计）：
    编写具有自我修复特性的程序，完整的本地和远程监控层次。

- High Performance（高性能）：
    单机每秒 5000万 吞吐。较优的内存使用：每 1GB 堆内在可生成约 250万 个 actor。 

- Elastic & Decentralized（弹性和去中心化）：
    自适应的集群管理、负载均衡、路由、分区和分版。

- Extensible（可扩展）：
    使用 **Akka Extensions** 来扩展 Akka ，以满足你的需要。

**Akka HTTP**

Akka HTTP 基于 `akka-actor` 和 `akka-stream` 完整实现了 HTTP 服务器和客户端协议。它不是一个 Web框架，而是一个更通用的工具包。

*本书源码在：*

- [https://github.com/yangbajing/scala-web-development](https://github.com/yangbajing/scala-web-development)
- [http://git.oschina.net/yangbajing/scala-web-development](http://git.oschina.net/yangbajing/scala-web-development)

有问题、建议请到 [https://github.com/yangbajing/scala-web-development/issues](https://github.com/yangbajing/scala-web-development/issues) 提供指导。

## 作者

- 作者：杨景（羊八井）
- 邮箱：yangbajing at gmail com
- Weibo: @yangbajing
- 主页：http://yangbajing.me
- 微信公众号：yangbajing-garden
![yangbajing-garden](imgs/qrcode_for_gh_70b815e4a7cd_344.jpg)
