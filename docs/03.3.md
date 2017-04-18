# Akka HTTP 的通用抽象

HTTP 规范定义了大量概念和功能，它们是不特定于具体实现的。本节将介绍通用于 client/server　的 API。

- **HTTP Model**：包括常见的请求、响应、headers等结构，代码都在 `akka-http-core` 模块，它们是构建大多次 Akka HTTP API的基础。
- **URI Model**：根据 [RFC 3986](https://tools.ietf.org/html/rfc3986#section-1.1.2) 实现了 URI 解析规则。
- **Marshalling**：将高级（对象）结构转换成某种低级表示形式的过程，其它流行的名称有：**Serialization** 和 **Pickling**。
- **Unmarshalling**：将低级表示形式转换成某种高级（对象）结构的过程，其它流行的名称有：**Deserialization** 和 **Unpickling**。
- **Encoding / Decoding**：[HTTP spec](http://tools.ietf.org/html/rfc7231#section-3.1.2.1)定义了 `Content-Encoding`头，实现了 HTTP 消息的压缩、解压缩。如：`gzip`、`deflate`……
- **JSON Support**：根据 **marshalling** 和 **unmarshalling** 实现的 JSON 解析，默认提供了 `akka-http-spray-json` 模块。也可很容易的定义自己的 JSON 解析。
- **XML Support**：根据 **marshalling** 和 **unmarshalling** 实现的 [Scala XML](https://github.com/scala/scala-xml) 解析，默认提供了 `akka-http-xml` 模块。也可很容易的定义自己的 XML 解析。
- **Akka HTTP Timeouts**：内建多种超时机制来保护服务器免受恶意攻击和编程错误。用户代码中可使用配置选项或API来定制。

## HTTP Model

`akka-http-core` 提供了 HTTP 数据结构的核心，很多的地方（包括你自己的代码）都导入了这些数据结构。主要的类型有：

- `HttpRequest` 和 `HttpResponse`：主要的消息模型
- `headers`：这个 package 包含了所有的预定义 HTTP header 和支持和类型
- 支持的类型，比如：`Uri`、`HttpMethods`、`MediaTypes`、`StatusCodes`等

通常，一个实体模型表现为一个不可变类型（class 或 trait）。由 HTTP 规范定义的实体类型的预定义实例都放在同名类型名加个一个 `s` 的复数形式 object 里。

比如：

- 定义的 `HttpMethod` 在 `HttpMethods` object。
- 定义的 `HttpCharset` 在 `HttpCharsets` object。
- 定义的 `HttpEncoding` 在 `HttpEncodings` object。
- 定义的 `HttpProtocol` 在 `HttpProtocols` object。
- 定义的 `MediaType` 在 `MediaTypes` object。
- 定义的 `StatusCode` 在 `StatusCodes` object。


## URI Model

当尝试解析 `URI` 字符串时，Akka HTTP 将在内部创建 `URI` 的实例。下面是常见的一些有效的 URI 实例：

```scala
Uri("ftp://ftp.is.co.za/rfc/rfc1808.txt") shouldEqual
  Uri.from(scheme = "ftp", host = "ftp.is.co.za", path = "/rfc/rfc1808.txt")

Uri("http://www.ietf.org/rfc/rfc2396.txt") shouldEqual
  Uri.from(scheme = "http", host = "www.ietf.org", path = "/rfc/rfc2396.txt")

Uri("ldap://[2001:db8::7]/c=GB?objectClass?one") shouldEqual
  Uri.from(scheme = "ldap", host = "[2001:db8::7]", path = "/c=GB", queryString = Some("objectClass?one"))

Uri("mailto:John.Doe@example.com") shouldEqual
  Uri.from(scheme = "mailto", path = "John.Doe@example.com")

Uri("news:comp.infosystems.www.servers.unix") shouldEqual
  Uri.from(scheme = "news", path = "comp.infosystems.www.servers.unix")

Uri("tel:+1-816-555-1212") shouldEqual
  Uri.from(scheme = "tel", path = "+1-816-555-1212")

Uri("telnet://192.0.2.16:80/") shouldEqual
  Uri.from(scheme = "telnet", host = "192.0.2.16", port = 80, path = "/")

Uri("urn:oasis:names:specification:docbook:dtd:xml:4.1.2") shouldEqual
  Uri.from(scheme = "urn", path = "oasis:names:specification:docbook:dtd:xml:4.1.2")
```

## Marshalling

Marshalling 是将类型 A 的实例转换成类型 B 的实例，提供于 `Marshaller[A, B]`。Akka HTTP 预定义了很多转换的别名，可以简化日常开发：

```scala
type ToEntityMarshaller[T] = Marshaller[T, MessageEntity]
type ToByteStringMarshaller[T] = Marshaller[T, ByteString]
type ToHeadersAndEntityMarshaller[T] = Marshaller[T, (immutable.Seq[HttpHeader], MessageEntity)]
type ToResponseMarshaller[T] = Marshaller[T, HttpResponse]
type ToRequestMarshaller[T] = Marshaller[T, HttpRequest]
```

`Marshaller[A, B]` 本质上类似于 `A => Future[List[Marshalling[B]]]` 这样一个函数，这个签名比较复杂，下面我们来解析下它。

1. **`Future`**：这个很明显，Marshaller 不需要同步的产生一个结果，我们可以异步的进行这个编码过程。
2. **`List`**：列表，说明对于给定类型 A ，不到一个 Marshaller 被作用于它。比如对于：`ToEntityMarshaller[OrderConfirmation]`，请求可
能使用 JSON 或 XML 格式，客户端可以添加 `Accept` 请求头来决定使用哪个 Marshaller。若客户端未指定，则在序列化时将使用第一个。
3. **`Marshalling[B]`**：这里没有直接返回类型 B 的实例，而是返回一个 `Marshalling[B]`。这使得在编码过程中查询 `MediaType` 和检查可
能的 `HttpCharset` 成为了可能，这样的条件都匹配后才会触发编码工作。在启用了这样的类型协商的方式之外，这种设计还允许将 marshalling 对目标实
例的构造延迟到真正需要的时刻。

**预定义 Marshallers**

Akka HTTP 已经包含大量预定义 marshallers：

* [PredefinedToEntityMarshallers](http://github.com/akka/akka-http/tree/v10.0.5/akka-http/src/main/scala/akka/http/scaladsl/marshalling/PredefinedToEntityMarshallers.scala)
    - `Array[Byte]`
    - `ByteString`
    - `Array[Char]`
    - `String`
    - `akka.http.scaladsl.model.FormData`
    - `akka.http.scaladsl.model.MessageEntity`
    - `T <: akka.http.scaladsl.model.Multipart`
* [PredefinedToResponseMarshallers](http://github.com/akka/akka-http/tree/v10.0.5/akka-http/src/main/scala/akka/http/scaladsl/marshalling/PredefinedToResponseMarshallers.scala)
    - `T`, 如果 `ToEntityMarshaller[T]` 可用
    - `HttpResponse`
    - `StatusCode`
    - `(StatusCode, T)`，如果 `ToEntityMarshaller[T]` 可用
    - `(Int, T)`，如果 `ToEntityMarshaller[T]` 可用
    - `(StatusCode, immutable.Seq[HttpHeader], T)`，如果 `ToEntityMarshaller[T]` 可用
    - `(Int, immutable.Seq[HttpHeader], T)`，如果 `ToEntityMarshaller[T]` 可用
* [PredefinedToRequestMarshallers](http://github.com/akka/akka-http/tree/v10.0.5/akka-http/src/main/scala/akka/http/scaladsl/marshalling/PredefinedToRequestMarshallers.scala)
    - `HttpRequest`
    - `Uri`
    - `(HttpMethod, Uri, T)`，如果 `ToEntityMarshaller[T]` 可用
    - `(HttpMethod, Uri, immutable.Seq[HttpHeader], T)`，如果 `ToEntityMarshaller[T]` 可用
* [GenericMarshallers](http://github.com/akka/akka-http/tree/v10.0.5/akka-http/src/main/scala/akka/http/scaladsl/marshalling/GenericMarshallers.scala)
    - `Marshaller[Throwable, T]`
    - `Marshaller[Option[A], B]`，如果 `Marshaller[A, B]` 和 `EmptyValue[B]` 可用
    - `Marshaller[Either[A1, A2], B]`，如果 `Marshaller[A1, B]` 和 `Marshaller[A2, B]` 可用
    - `Marshaller[Future[A], B]`，如果 `Marshaller[A, B]` 可用
    - `Marshaller[Try[A], B]`，如果 `Marshaller[A, B]` 可用

**隐式解析**

Akka HTTP 的 marshalling 基础设施使用基于 **class** 的方式，这意味着 Marshaller 从一个特定 A 类型转换到 B 类型实例是隐式可用的。

Akka HTTP 预定义 `Marshaller` trait，它们的定义都在同名伴生对象中，这意味着代码中不需要显示导入即可用。同时，你也可以定义自己的版本来覆盖它。

```scala
object Marshaller
  extends GenericMarshallers
  with PredefinedToEntityMarshallers
  with PredefinedToResponseMarshallers
  with PredefinedToRequestMarshallers {
  ...
}
```

**使用 Marshallers**

可以在代码中直接使用 Marshallers，`akka.http.scaladsl.marshalling.Marshal` object 是一个很好的入口：

```scala
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._

import system.dispatcher // ExecutionContext

val string = "Yeah"
val entityFuture = Marshal(string).to[MessageEntity]
val entity = Await.result(entityFuture, 1.second) // 非测试代码中不要这么使用！
entity.contentType shouldEqual ContentTypes.`text/plain(UTF-8)`

val errorMsg = "Easy, pal!"
val responseFuture = Marshal(420 -> errorMsg).to[HttpResponse]
val response = Await.result(responseFuture, 1.second) // 非测试代码中不要这么使用！
response.status shouldEqual StatusCodes.EnhanceYourCalm
response.entity.contentType shouldEqual ContentTypes.`text/plain(UTF-8)`

val request = HttpRequest(headers = List(headers.Accept(MediaTypes.`application/json`)))
val responseText = "Plaintext"
val respFuture = Marshal(responseText).toResponseFor(request) // 根据请求来决定使用哪个 Marshaller
a[Marshal.UnacceptableResponseContentTypeException] should be thrownBy {
  Await.result(respFuture, 1.second) // 客户端请求 JSON 格式，但这里只提供了 `text/plain` 的编码方式
}
```

## Unmarshalling

Unmarshalling 将类型 A 的实例转换成类型 B，`Unmarshaller[A, B]`。Akka HTTP 预定义了很多转换的别名，可以简化日常开发：

```scala
type FromEntityUnmarshaller[T] = Unmarshaller[HttpEntity, T]
type FromMessageUnmarshaller[T] = Unmarshaller[HttpMessage, T]
type FromResponseUnmarshaller[T] = Unmarshaller[HttpResponse, T]
type FromRequestUnmarshaller[T] = Unmarshaller[HttpRequest, T]
type FromByteStringUnmarshaller[T] = Unmarshaller[ByteString, T]
type FromStringUnmarshaller[T] = Unmarshaller[String, T]
type FromStrictFormFieldUnmarshaller[T] = Unmarshaller[StrictForm.Field, T]
```

核心的 `Unmarshaller[A, B]` 类型非常类似 `A => Future[B]` 函数，它提供了一个解码的转换流程。

**预定义 Unmarshallers**

Akka HTTP 已经预定义了大量的 marshallers，我们可以在代码中直接使用：

* [PredefinedFromStringUnmarshallers](http://github.com/akka/akka-http/tree/v10.0.5/akka-http/src/main/scala/akka/http/scaladsl/unmarshalling/PredefinedFromStringUnmarshallers.scala)
    - `Byte`
    - `Short`
    - `Int`
    - `Long`
    - `Float`
    - `Double`
    - `Boolean`
* [PredefinedFromEntityUnmarshallers](http://github.com/akka/akka-http/tree/v10.0.5/akka-http/src/main/scala/akka/http/scaladsl/unmarshalling/PredefinedFromEntityUnmarshallers.scala)
    - `Array[Byte]`
    - `ByteString`
    - `Array[Char]`
    - `String`
    - `akka.http.scaladsl.model.FormData`
* [GenericUnmarshallers](http://github.com/akka/akka-http/tree/v10.0.5/akka-http/src/main/scala/akka/http/scaladsl/unmarshalling/GenericUnmarshallers.scala)
    - `Unmarshaller[T, T]` (identity unmarshaller)
    - `Unmarshaller[Option[A], B]`，如果 `Unmarshaller[A, B]` 可用
    - `Unmarshaller[A, Option[B]]`, 如果 `Unmarshaller[A, B]` 可用

**隐式解析**

Akka HTTP 的 unmarshalling 基础设施使用基于 **class** 的方式，这意味着 Unmarshaller 从一个特定 A 类型转换到 B 类型实例是隐式可用的。

Akka HTTP 预定义 `Unmarshaller` trait，它们的定义都在同名伴生对象中，这意味着代码中不需要显示导入即可用。同时，你也可以定义自己的版本来覆
盖它。

```scala
object Unmarshaller
  extends GenericUnmarshallers
  with PredefinedFromEntityUnmarshallers
  with PredefinedFromStringUnmarshallers {
  ....
}
```

**使用 Unmarshallers**

在 Akka HTTP 中有许多地方隐式的使用了 Unmarshallers ，比对 Routing DSL 里面的 `entity(as[T])`。但是，也可以显示的使用它，一个很好的切
入点是：`akka.http.scaladsl.unmarshalling.Unmarshal` object，可以如下使用：

```scala
import akka.http.scaladsl.unmarshalling.Unmarshal
import system.dispatcher // Optional ExecutionContext (default from Materializer)
implicit val materializer: Materializer = ActorMaterializer()

import scala.concurrent.Await
import scala.concurrent.duration._

val intFuture = Unmarshal("42").to[Int]
val int = Await.result(intFuture, 1.second) // don't block in non-test code!
int shouldEqual 42

val boolFuture = Unmarshal("off").to[Boolean]
val bool = Await.result(boolFuture, 1.second) // don't block in non-test code!
bool shouldBe false
```

## Encoding / Decoding

当前 Akka HTTP 支持 `gzip` 和 `deflate` 格式的压缩、解压缩，它们的代码逻辑都在 `akka.http.scaladsl.coding` 包中。

## JSON Support

Akka HTTP 包含了一个 `akka-http-spary-json` 模块，它实现了完整的 JSON 支持。

**JacksonSupport**

TODO 本文将主要使用 Jackson 来实现 JSON 解析处理。

## XML Support

**Scala XML支持**

Scala语言内建了 XML 支持。 [ScalaXmlSupport](http://github.com/akka/akka-http/tree/v10.0.5/akka-http-marshallers-scala/akka-http-xml/src/main/scala/akka/http/scaladsl/marshallers/xml/ScalaXmlSupport.scala) trait　
提供了 `FromEntityUnmarshaller[NodeSeq]` 和 `ToEntityMarshaller[NodeSeq]`，可以在代码中直接使用。

1. 添加库依赖：`"com.typesafe.akka" %% "akka-http-xml" % "1.x"`
2. 引入隐式转换 `import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._`，或混入 `akka.http.scaladsl.marshallers.xml.ScalaXmlSupport` trait。

## Akka HTTP Timeouts

### 通用超时

`idle-timeout` （空闲超时）是一个全局配置项，设置给定连接的最大非活动时间。若一个连接是打开的，但在给定时间内没有请求/响应数据写入，则连接将
被自动关闭。它们有如下配置项：

- `akka.http.server.idle-timeout`
- `akka.http.client.idle-timeout`
- `akka.http.host-connection-pool.idle-timeout`
- `akka.http.host-connection-pool.client.idle-timeout`

### 服务端超时

**请求超时**

请求超时设置服务端产生一个 `HttpResponse` 响应的最大超时时间，若超时到，则服务端将自动注入一个 **Unavailable HTTP** 响应并关闭连接。默认
的超时时间是 20秒，可通过 `akka.http.server.request-timeout` 进行配置。

**Bind timeout（绑定超时）**

在给定时间内，使用 TCP 协议绑定 address/port 的进程必需完成（任何 `Http().bind*` 方法），可以使用 `akka.http.server.bind-timeout` 
设置。

**Linger超时**

Linger（逗留）超时是指服务端的所有数据被传递到网络层后还保持连接打开的时间。这个设置类似 **SO_LINGER** 套接字（Socket）选项，但这里还包括了 
Akka IO 和 Akka Streams 网络栈。这是一个额外的预防措施，防止客户端长时间保持打开服务端已被认为完成了的连接。

### 客户端超时

**连接超时**

连接超时是客户端通过 TCP 协议连接上服务端必须完成的时间，通常不需要修改它。但可以使用 `akka.http.client.connecting-timeout` 进行自定义。
