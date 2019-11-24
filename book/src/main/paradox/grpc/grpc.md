# gRPC服务

## 定义消息和服务

@@snip [greeter.proto](../../../../../grpc/src/main/protobuf/greeter/greeter.proto)

这里定义了两个消息：`HelloRequest`、`HelloReply`和`GreeterService`服务，`GreeterService`定义了4个服务方法，分别是：

- `SayHello`：经典的请求-响应服务，发送一个请求获得一个响应；
- `ItKeepsTalking`：持续不断的发送多个请求，在请求停止后获得一个响应；
- `ItKeepsReplying`：发送一个请求，获得持续不断的多个响应；
- `StreamHelloes`：持续不断的发送响应的同时也可获得持续不断的响应，可以通过`Source.queue`来获得可发送数据的`Queue`和获得响应数据的`Source`。

## 实现 gRPC 服务

@@snip [GreeterServiceImpl](../../../../../grpc/src/main/scala/greeter/GreeterServiceImpl.scala) { #GreeterServiceImpl }

@@@note
有关Akka Streams的更多内容可阅读作者写的另一本开源电子书： [Akka Cookbook](https://yangbajing.gitee.io/akka-cookbook/streams/) 。
@@@

## 测试 gRPC 服务

通过 Scalatest 对实现的4个gRPC服务进行测试，下面是单元测试代码：

@@snip [GreeterClientTest](../../../../../grpc/src/test/scala/greeter/GreeterClientTest.scala) { #GreeterServiceClient }

在运行测试前需要先启动gRPC服务，在 Scalatest 的`beforeAll`函数内启动gRPC HTTP 2服务：

@@snip [GreeterClientTest](../../../../../grpc/src/test/scala/greeter/GreeterClientTest.scala) { #GreeterService }

在构造 `GreeterServiceClient` gRCP客户端时需要提供`GrpcClientSettings`设置选项，这里通过调用`fromConfig`函数来从 **HOCON** 配置文件里读取gRPC服务选项，相应的`application-test.conf`配置文件内容如下：

@@snip [application-test.conf](../../../../../grpc/src/test/resources/application-test.conf)

其中`use-tls`设置gRPC客户端不使用HTTPs建立连接，因为我们这个单元测试启动的gRPC HTTP服务不未启动SSL/TLS。
