# Akka gRPC

gRPC使用Protobuf进行数据序列化，基于HTTP 2提供RPC通信。具有快速、高效、易用、可扩展等特点。采用HTTP 2作为底层协议，可以更好的与已有的HTTP基础服务整合，简化了运维管理（不需要为了RPC单独开放网络端口，并对其进行管理）。gRPC支持请求/响应和Stream多种接口形式，可以实现服务端推送功能。

Akka提供了开箱即用的 [akka-grpc](https://doc.akka.io/docs/akka-grpc/current/)，从编译、构建、发布……与Scala/Akka生态完美整合。[**Why gRPC**](https://doc.akka.io/docs/akka-grpc/current/whygrpc.html) 这篇文章详细的说明了为什么需要gRPC，特别是gRPC与REST、SOAP、Message Bus和Akka Remoting的区别，阐述的简明扼要。

@@toc { depth=2 }

@@@ index

* [grpc](grpc.md)
* [build tool](build-tool.md)
* [deployment](deployment.md)
* [小结](grpc.z.md)

@@@
