# Directive 指令

**指令** 是用于创建任意复杂路由结构的小型构建块，Akka HTTP已经预先定义了大部分指令，当然我们也可以很轻松的定义自己的指令。

## 指令基础

通过指令来创建路由，需要理解指令是如何工作的。我们先来看看指令和原始的`Route`的对比。因为`Route`只是函数的类型别名，所有`Route`实例可以任何方式写入函数实例，如作为函数文本：

```scala
val route: Route = { ctx => ctx.complete("yeah") }  // 或者可简写为：_.complete("yeah")
```

而`complete`指令将变得更短：

```scala
val route: Route = complete("yeah")
```

`complete`指令定义如下：

```scala
def complete(m: => ToResponseMarshallable): StandardRoute =
  StandardRoute(_.complete(m))

abstract class StandardRoute extends Route {
  def toDirective[L: Tuple]: Directive[L] = StandardRoute.toDirective(this)
}

object StandardRoute {
  def apply(route: Route): StandardRoute = route match {
    case x: StandardRoute => x
    case x                => new StandardRoute { def apply(ctx: RequestContext) = x(ctx) }
  }
}
```

## 指令可以做什么？

指令用来灵活、高效的构造路由结构，简单来说它可以做如下这些事情：

1. 将`Route`传入的请求上下文`RequestContext`转换为内部路由需要的格式（修改请求）。
    ```scala
    mapRequest(request => request.withHeaders(request.headers :+ RawHeader("custom-key", "custom-value")))
    ```
2. 根据设置的逻辑来过滤`RequestContext`，符合的通过（pass），不符合的拒绝（reject）。
    ```scala
    path("api" / "user" / "page")
    ```
3. 从`RequestContext`中提取值，并使它在内部路径内的路由可用。
    ```scala
    extract(ctx => ctx.request.uri)
    ```
4. 定义一些处理逻辑附加到`Future[RouteRoute]`的转换链上，可用于修改响应或拒绝。
    ```scala
    mapRouteResultPF {
      case RouteResult.Rejected(_) =>
        RouteResult.Complete(HttpResponse(StatusCodes.InternalServerError))
    }
    ```
5. 完成请求（使用`complete`）
    ```scala
    complete("OK")
    ```

指令已经包含了路由（`Route`）可以用的所有功能，可以对请求和响应进行任意复杂的转换处理。

## 组合指令

Akka HTTP提供的Routing DSL构造出来的路由结构是一颗树，所以编写指令时通常也是通过“嵌套”的方式来组装到一起的。看一个简单的例子：

@@snip [RouteExample.scala](../../scala/book/example/route/RouteExample.scala) { #basic-route-tree }

Akka HTTP提供的Routing DSL以树型结构的方式来构造路由结构，它与 **Playframework** 和 **Spring** 定义路由的方式不太一样，很难说哪一种更好。也许刚开始时你会不大习惯这种路由组织方式，一但熟悉以后你会认为它非常的有趣和高效，且很灵活。

可以看到，若我们的路由非常复杂，它由很多个指令组成，这时假若还把所有路由定义都放到一个代码块里实现就显得非常的臃肿。因为每一个指令都是一个独立的代码块，它通过函数调用的形式组装到一起，我们可以这样对上面定义的路由进行拆分。

@@snip [RouteExample.scala](../../scala/book/example/route/RouteExample.scala) { #basic-route-1 }

通过`&`操作符将多个指令组合成一个，所有指令都符合时通过。

@@snip [RouteExample.scala](../../scala/book/example/route/RouteExample.scala) { #directive-and }

通过`|`操作符将多个指令组合成一个，只要其中一个指令符合则通过。

@@snip [RouteExample.scala](../../scala/book/example/route/RouteExample.scala) { #directive-or }

@@@ note
上面这段代码来自真实的业务，因为某些落后于时代的安全原因，网管将HTTP的PUT、DELETE、HEAD等方法都禁用了，只保留了GET、POST两个方法。使用如上的技巧可以同时支持两种方式来访问路由。

**还有一种方案来解决这个问题**

@@snip [RouteExample.scala](../../scala/book/example/route/RouteExample.scala) { #deleteUser2 }

客户端不需要修改访问地址为 `/user/{userId}/_delete`，它只需要这样访问路由 `POST /user/{userId}?httpMethod=DELETE`。`overrideMethodWithParameter("httpMethod")`会根据`httpMethod`参数的值来将请求上下文里的`HttpRequest.method`转换成 **DELETE** 方法请求。
@@@

@@@ warning
可以看到，将多个指令组合成一个指令可以简化我们的代码。但是，若过多地将几个指令压缩组合成一个指令，可能并不会得到易读、可维护的代码。
@@@

## 使用`concat`来连接多个指令

除了通过`~`链接操作符来将各个指令连接起来形成路由树，也可以通过`concat`指令来将同级路由（指令）连接起来（子路由还是需要通过嵌套的方式组合）。
```scala
val route: Route = concat(a, b, c) // 等价于 a ~ b ~ c
```

## 类型安全的指令

当使用`&`和`|`操作符组合多个指令时，Routing DSL将确保其按期望的方式工作，并且还会在编译器检查是否满足逻辑约束。下面是一些例子：

```scala
val route1 = path("user" / IntNumber) | get // 不能编译
val route2 = path("user" / IntNumber) | path("user" / DoubleNumber) // 不能编译
val route3 = path("user" / IntNumber) | parameter('userId.as[Int]) // OK

// 组合指令同时从URI的path路径和查询参数时获取值
val pathAndQuery = path("user" / IntNumber) & parameters('status.as[Int], 'type.as[Int])
val route4 = pathAndQuery { (userId, status, type) =>
    ....
  }
```

## 指令类型参数里的 Tuple （自动拉平 flattening）

```scala
abstract class Directive[L](implicit val ev: Tuple[L])

type Directive0 = Directive[Unit]
type Directive1[T] = Directive[Tuple1[T]]
```

指令的定义，它是一个泛型类。参数类型`L`需要可转化成`akka.http.scaladsl.server.util.Tuple`类型（即Scala的无组类型，TupleX）。下面是一些例子，DSL可以自动转换参数类型为符合的`Tuple`。

```scala
val futureOfInt: Future[Int] = Future.successful(1)
val route =
  path("success") {
    onSuccess(futureOfInt) { //: Directive[Tuple1[Int]]
      i => complete("Future was completed.")
    }
  }
```

`onSuccess(futureOfInt)`将返回值自动转换成了`Directive[Tuple1[Int]]`，等价于`Directive1[Int]`。

```scala
val futureOfTuple2: Future[Tuple2[Int,Int]] = Future.successful( (1,2) )
val route =
  path("success") {
    onSuccess(futureOfTuple2) { //: Directive[Tuple2[Int,Int]]
      (i, j) => complete("Future was completed.")
    }
  }
```

`onSuccess(futureOfTuple2)`返回`Directive1[Tuple2[Int, Int]]`，等价于`Directive[Tuple1[Tuple2[Int, Int]]]`。但DSL将自动转换成指令`Directive[Tuple2[Int, Int]]`以避免嵌套元组。

```scala
val futureOfUnit: Future[Unit] = Future.successful( () )
val route =
  path("success") {
    onSuccess(futureOfUnit) { //: Directive0
      complete("Future was completed.")
    }
  }
```

对于`Unit`，它比较特殊。`onSuccess(futureOfUnit)`返回`Directive[Tuple1[Unit]]`。DSL将会自动转换为`Directive[Unit]`，等价于`Directive0`。
