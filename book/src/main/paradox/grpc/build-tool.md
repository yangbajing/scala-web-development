# 构建工具

## sbt 配置

```sbt
project
  .in(file("grpc"))
  .enablePlugins(AkkaGrpcPlugin, JavaAgent)
  .settings(
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "runtime;test",
    libraryDependencies ++= Seq(
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"))
```

要使工程支持 Akka gRPC，需要在sbt项目里启用`AkkaGrpcPlugin`插件，若需要在sbt里测试gRPC服务，还需要同时启用`JavaAgent`插件。

`jetty-alpn-agent`提供Akka HTTP 2需要的 **ALPN** 支持，使用 `javaAgents` 配置项使它在`runtime`和`test`两个执行范围可用。

当你需要在代码中引用`google.proto`或`scalapb.proto`定义的消息Protobuf类型时，需要引入`scalapb-runtime`库依赖。

## 目录结构

在一个 sbt 目录结构里，通过定义`.proto`定义的Protobuf消息和gRPC服务需要放在`protobuf`（或`proto`）目录，如下面目录结构：

```
├── src
│   ├── main
│   │   ├── protobuf
│   │   ├── resources
│   │   └── scala
│   └── test
│       ├── resources
│       └── scala
```

通过`.proto`定义的消息类型和gRPC服务，会在sbt的托管源码路径下生成相应的消息`case class`、服务接口和客户端实现：

```
└── target
    ├── scala-2.13
    │   ├── src_managed
    │   │   └── main
    │   │       ├── greeter
    │   │       │   ├── GreeterProto.scala
    │   │       │   ├── GreeterServiceClient.scala
    │   │       │   ├── GreeterServiceHandler.scala
    │   │       │   ├── GreeterService.scala
    │   │       │   ├── HelloReply.scala
    │   │       │   └── HelloRequest.scala
```
