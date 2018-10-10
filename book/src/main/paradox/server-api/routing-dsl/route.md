# Route 路由

```
type Route = RequestContext => Future[RouteResult]
```

Akka HTTP 里路由是类型 `Route` 只是一个类型别名，它实际上是一个函数 `RequestContext => Future[RouteResult]`，它接受一个 `RequestContext` 参数，并返回 `Future[RouteResult]`。`RequestContext`保存了每次HTTP请求的上下文，包括`HttpRequest`、`unmatchedPath`、`settings`等请求资源，还有4个函数来响应数据给客户端：

- `def complete(obj: ToResponseMarshallable): Future[RouteResult]`：请求正常完成时调用，返回数据给前端。通过 **Marshal** 的方式将用户响应的数据类型转换成 `HttpResponse`，再赋值给`RouteResult.Complete`。
- `def reject(rejections: Rejection*): Future[RouteResult]`：请求不能被处理时调用，如：路径不存、HTTP方法不支持、参数不对、Content-Type不匹配等。也可以自定义`Rejection`类型。
- `def redirect(uri: Uri, redirectionType: Redirection): Future[RouteResult]`：用指定的url地址和给定的HTTP重定向响应状态告知客户端需要重定向的地址和方式。`redirect`实际上是对`complete`的封装，可以通过向`complete`函数传入指定的`HttpResponse`实例实现：
    ```scala
    complete(HttpResponse(
      status = redirectionType,
      headers = headers.Location(uri) :: Nil,
      entity = redirectionType.htmlTemplate match {
        case ""       => HttpEntity.Empty
        case template => HttpEntity(ContentTypes.`text/html(UTF-8)`, template format uri)
      }))
    ```
- `def fail(error: Throwable): Future[RouteResult]`：将给定异常实例气泡方式向上传递，将由最近的`handleExceptions`指令和`ExceptionHandler`句柄处理该异常（若异常类型是`RejectionError`，将会被包装成`Rejection`来执行）。

## RequestContext

`RequestContext`包装了HTTP请求的实例`HttpRequest`和运行时需要的一些上下文信息，如：`ExcutionContext`、`Materializer`、`LoggingAdapter`、`RoutingSettings`等，还有`unmatchedPath`，该值描述了请求UIR还未被匹配的路径。

@@@ note { title='unmatchedPath' }
若请求URI地址为：`/api/user/page`，对于如下路由定义`unmatchedPath`将为 `/user/page`。
```scala
  pathPrefix("api") { ctx =>
    // ctx.unmatchedPath 等价于 "/user/page"
    ctx.complete(ctx.request.uri.path.toString())
  }
```
@@@

## RouteResult

`RouteResult`是一个简单的ADT（抽象数据类型），对路由执行后可能的结果进行建模，定义为：

```scala
sealed trait RouteResult extends javadsl.server.RouteResult

object RouteResult {
  final case class Complete(response: HttpResponse) extends javadsl.server.Complete with RouteResult {
    override def getResponse = response
  }
  final case class Rejected(rejections: immutable.Seq[Rejection]) extends javadsl.server.Rejected with RouteResult {
    override def getRejections = rejections.map(r => r: javadsl.server.Rejection).toIterable.asJava
  }
}
```

通常不需要我们直接创建`RouteResult`实例，而是通过预定义的指令`RouteDirectives`定义的函数（`complete`、`reject`、`redirect`、`fail`）或`RequestContext`上的方法来创建。

## 组合路由

将单个的路由组合成一个复杂的路由结构一般有3种方法：

1. **路由转换（嵌套）**，将请求委托给另一个“内部”路由，在此过程中可以更改传请求和输出结果的某些属性。
2. **过滤路由**，只允许满足给定条件的路由通过。
3. **链接路由**，若给定的第一个路由被拒绝（reject），将尝试第二个路由，并依次类推。通过级联操作符`~`来实现，导入`akka.http.scaladsl.server.Directvies._`后可用。

前两种方法可由指令（Directive）提供，Akka HTTP已经预告定义了大量开箱即用的指令，也可以自定义我们自己的指令。通过指令这样的机制，使得Akka HTTP的路由定义异常强大和灵活。

## 路由树

当通过嵌套和链接将指令和自定义路由组合起来构建成一个路由结构时，将形成一颗树。当一个HTTP请求进入时，它首先被注入的树的根，并以深入优先的方式向下流径所有分支，直到某个节点完成它（返回`Future[RouteResult.Complete]`）或者完全拒绝它（返回`Future[RouteResult.Rejected]`）。这种机制可以使复杂的路由匹配逻辑可以非常容易的实现：简单地将最特定的情况放在前面，而将一般的情况放在后面。

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

上面这个例子：

- **route 1** 只有当a、b、c都通过时才会到达。
- **route 2** 只有当a、b通过，但c被拒绝时才会到达。
- **route 3** 只有当a、b通过，但c、d和它之前的所有链接的路由都被拒绝时才会到达。
    - *可以被看作一个捕获所有（catch-all）的默认路由，之后会看到我们将利用此特性来实现服务端对SPA前端应用的支持。*
- **route 4** 只有当a通过，b和其所有子节点都被拒绝时才会到达。
