# 自定义指令

有3种创建自定义指令的基本方法：

1. 将已有指令通过命名配置（比如通过组合的方式）的方式来定义新的指令
2. 转换已存在的指令
3. 从头开始实现一个指令

## 命名配置

创建自定义指令最简便的方法就是将一个或多个已有指令通过配置的方式分配一个新的名字来定义。事实上Akka HTTP预定义的大多数指令都由以较低级别指令命名配置的方式来定义的。如：

```scala
val getPut = get & put

def postEntity[T](um: FromRequestUnmarshaller[T]): Directive1[T] = post & entity(um)

def completeOk: Route = complete(HttpEntity.Empty)

def completeNotImplemented: Route = complete(StatusCodes.NotImplemented)
```

## 转换已存在的指令

第二种方式是通过“转换方法”来转换现有指令，这是在`Directive`类上定义的方法：

- `map/tmap`
- `flatMap/tflatMap`
- `require/trequire`
- `recover/recoverPF`

### map、tmap

`map、tmap`就和Scala集合库上的`map`转换类似，它可以将值映射转换成另一个值。`map`用于`Directive1`类型的指令（单值指令），而`tmap`用于值为其它元组的情况，它的签名如下：

```scala
def tmap[R](f: L => R): Directive[Out]
```

`tmap`可以用来将提取的元组转换成另一个元组，提取的数量和类型都可以改变，而`map`只用改变变换后的类型。如下是一个虚构的例子：

```scala
val twoIntParameters: Directive[(Int, Int)] =
  parameters(("a".as[Int], "b".as[Int]))

val myDirective: Directive1[String] =
  twoIntParameters.tmap {
    case (a, b) => (a + b).toString
  }

// tests:
Get("/?a=2&b=5") ~> myDirective(x => complete(x)) ~> check {
  responseAs[String] mustBe "7"
}
```

### flatMap、tflatMap

通过`map、tmap`可以将指令抽取的值转换成其它值，但不能改变其“抽取”的性质。当需要抽取一个对它做一些转换操作，并将结果交给一个嵌套的指令使用时，`map、tmap`就无能为力了。同`map、tmap`类似，`flatMap`也是用于单值指令，而`tflatMap`用于其它元组值。`tflatMap`的函数签名如下：

```scala
def tflatMap[R: Tuple](f: L => Directive[R]): Directive[R]
```

可以看一个例子，预定义的`method`指令，它的定义如下：

```scala
def method(httpMethod: HttpMethod): Directive0 =
  extractMethod.flatMap[Unit] {
    case `httpMethod` => pass
    case _            => reject(MethodRejection(httpMethod))
  } & cancelRejections(classOf[MethodRejection])

val get: Directive0 = method(HttpMethods.GET)
val post: Directive0 = method(HttpMethods.POST)
```

1. 通过调用`extractMethod`指令获取请求的HTTP方法，再通过`flatMap[Unit]`转换方法对它进行处理。因为`extractMethod`是一个单值指令且转换后值为`Unit`（也是个单值），这里调用`flatMap`方法。
2. 当请求的实际HTTP方法与传入参数`httpMethod`匹配时，调用`pass`指令使其通过，否则调用`reject(MethodRejection(httpMethod))`拒绝。

### require、trequire

require方法将单个指令转换为没有抽取值的指令，该指令根据谓词函数过滤请求，所有谓词函数调用后为false的请求都被拒绝，其它请求保持不变。它的定义如下：

```scala
def require(predicate: T => Boolean, rejections: Rejection*): Directive0 =
  underlying.filter(predicate, rejections: _*).tflatMap(_ => Empty)
```

从定义可以看出，它实际上是先通过谓词函数调用`filter`方法对请求进行过滤，然后再调用`tflatMap`函数将指令抽取的值去掉。

### recover、recoverPF

recover方法允许“捕获”由底层指令向上冒泡产生的rejections，并生成且有相同抽取类型的替代指令。这样就可以恢复指令来通过而不是拒绝它。它们的定义分别如下：

```scala
def recover[R >: L: Tuple](recovery: immutable.Seq[Rejection] => Directive[R]): Directive[R] =
  Directive[R] { inner => ctx =>
    import ctx.executionContext
    @volatile var rejectedFromInnerRoute = false
    tapply({ list => c => rejectedFromInnerRoute = true; inner(list)(c) })(ctx).fast.flatMap {
      case RouteResult.Rejected(rejections) if !rejectedFromInnerRoute => recovery(rejections).tapply(inner)(ctx)
      case x => FastFuture.successful(x)
    }
  }

def recoverPF[R >: L: Tuple](recovery: PartialFunction[immutable.Seq[Rejection], Directive[R]]): Directive[R] =
  recover { rejections => recovery.applyOrElse(rejections, (rejs: Seq[Rejection]) => RouteDirectives.reject(rejs: _*)) }
```

we

## 从头开始实现一个指令

可以通过调用`Directive.apply`或它的子类型来从头开始定义一个指令，`Directive`的简化定义看起来像下面这样：

```scala
abstract class Directive[L](implicit val ev: Tuple[L]) {
  def tapply(f: L => Route): Route
}

object Directive {

  /**
   * Constructs a directive from a function literal.
   */
  def apply[T: Tuple](f: (T => Route) => Route): Directive[T] =
    new Directive[T] { def tapply(inner: T => Route) = f(inner) }

}
```

`Directive`类型有一个抽象方法`tapply`，参数`f`是一个函数类型，将类型`L`传入并返回`Route`。

对于一个抽取访问host的port的指令，可以这样实现：

```scala
def hostnameAndPort: Directive[(String, Int)] = Directive[(String, Int)] { inner => ctx =>
  val authority = ctx.request.uri.authority
  inner((authority.host.address(), authority.port))(ctx)
}
```
