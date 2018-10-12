# 常用指令（Directives）

Akka HTTP已经预定义了大量的指令，应用开发时可以直接使用。若现存的指令不能满足我们的需求，Akka HTTP也提供了自定义指令的方法。

Akka HTTP的所有预定义指令都可以通过混入`Directives` trait或导入`Directives._`来访问。

```scala
class MyRoute extends Directives {
  ....
}

class MyRoute {
  import Directives._
  ....
}
```

`Directives`按功能分成了很多经类，完成的指令分类和说明见官方文档：[https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/by-trait.html](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/by-trait.html)。这里我们着重介绍下日常开发工作中经常使用到的指令。

## PathDirectives（路径指令）

以指定的路径与`RequestContext.unmatchedPath`进行匹配，当匹配成功时可选提取一个或多个值（提取值的类型由指定的路径参数决定）。若匹配失败，将调用`reject`拒绝此次请求。它处理的类型为：`Uri.Path`（之后简称`Path`）。

*官方文档：[https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/path-directives/index.html](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/path-directives/index.html)*

最常会用到的路径指令有：`pathPrefix`、`path`、`pathEnd`和`pathEndOrSingleSlash`。

`pathPrefix`：对于路径：`/user/page`，指令`pathPrefix("user")`将先与第一个'/'匹配，再匹配`user`，然后将剩余部分`/page`设置到`RequestContext.unmatchedPath`。`pathPrefix(X)`只与路径的前缀部分匹配`X`。

`path`：对于路径：`/page`，指令`path("page")`将先与'/'匹配，再匹配`page`，同时剩余部分应为空，它将`RequestContext.unmatchedPath`设置为`Path.Empty`。`path(X)`会与路径进行完整匹配。

`pathEnd`：`pathEnd`是一个没有参数的指令，将在剩余路径为`Path.Empty`时匹配成功。它常用于在嵌套路由下匹配父路由已经完全匹配的路径（`unmatchedPath`为`Path.Empty`）。

`pathEndOrSingleSlash`：类似`pathEnd`，但它还会在剩余路径为`Slash`（路径为'/'）时也匹配成功。

@@@ note { title='注意' }
对于希望只匹配URI的一部分，而将剩余部分委托给嵌套路由（子路由）时必需使用`pathPrefix`指令。只有当确定嵌套部分不再对URI进行匹配操作时才使用`path`指令。因此，将一个`path`或`path`指令嵌套在一个`path`指令下，它是永远不会被匹配上的。因为这时候`RequestContext.unmatchedPath`为空。
@@@

### Path示例

```scala
// 路由：
val route =
  path("foo") {
    complete("/foo")
  } ~
    path("foo" / "bar") {
      complete("/foo/bar")
    } ~
    pathPrefix("ball") {
      pathEnd {
        complete("/ball")
      } ~
        path(IntNumber) { int =>
          complete(if (int % 2 == 0) "even ball" else "odd ball")
        }
    }

// 测试：
Get("/") ~> route ~> check {
  handled shouldEqual false
}

Get("/foo") ~> route ~> check {
  responseAs[String] shouldEqual "/foo"
}

Get("/foo/bar") ~> route ~> check {
  responseAs[String] shouldEqual "/foo/bar"
}

Get("/ball/1337") ~> route ~> check {
  responseAs[String] shouldEqual "odd ball"
}
```

### 深入`Uri.Path`

`Uri.Path`是一个递归数据结构，类似Scala集合类型`List`，它的简化版定义如下：
```scala
  sealed abstract class Path {
    type Head // head的类型由实现类定义，可能是`Char`，也可能是`String`
    def isEmpty: Boolean
    def startsWithSlash: Boolean
    def startsWithSegment: Boolean
    def endsWithSlash: Boolean
    def head: Head // 路径链表头
    def tail: Path // 路径链表剩余部分
    def length: Int // 整个路径链表长度，包括解码字符和路径段
    def charCount: Int // 整个路径链里解码字符的数量
    def ::(c: Char): Path = { require(c == '/'); Path.Slash(this) }
    def ::(segment: String): Path
    def +(pathString: String): Path = this ++ Path(pathString)
    def ++(suffix: Path): Path
    def /(segment: String): Path = this ++ Path.Slash(segment :: Path.Empty)
    def ?/(segment: String): Path = if (this.endsWithSlash) this + segment else this / segment
    def dropChars(count: Int): Path // 从链表头部开始，去看count个解码字符，同时解码字符间的路径段也一起被去掉
  }
```

@@@ note { title='名词解释' }
- **解码字符**：URI通过'/'符合分隔，路径需要使用'/'都需要对其进行encode。在Akka HTTP里抽象为`Slash`类型。
- **路径段**：URI字符串是被解码字符分隔出来的字符串。在Akka HTTP里抽象为`Segment`类型。
@@@

`Path`定义为抽象类，它的具体实现类有三个：`Empty`、`Slash`、`Segment`，而`Empty`继承于`SlashOrEmpty`。

- **SlashOrEmpty**：路径为解码字符'/'或空
    ```scala
    sealed abstract class SlashOrEmpty extends Path {
      def startsWithSegment = false
    }
    ```
- **Empty**：路径为空
    ```scala
    case object Empty extends SlashOrEmpty {
      type Head = Nothing
      def isEmpty = true
      def startsWithSlash = false
      def head: Head = throw new NoSuchElementException("head of empty path")
      def tail: Path = throw new UnsupportedOperationException("tail of empty path")
    }
    ```
- **Slash**：路径为解码字符'/'
    ```scala
    final case class Slash(tail: Path) extends SlashOrEmpty {
      type Head = Char
      def head = '/'
    }
    ```
- **Segment**：路径为字符串，URI里面实际的路径段。
    ```scala
    final case class Segment(head: String, tail: SlashOrEmpty) extends Path {
      if (head.isEmpty) throw new IllegalArgumentException("Path segment must not be empty")
      type Head = String
    }
    ```

### 深入`PathMatcher`

当一个HTTP请求（若确切地说一个`RequestContext`实例）进行路由结构时，它有一个与`request.uri.path`相同类型的`unmatchedPath`。当`RequestContext`通过路由树向下传播，通过一个或多个`pathPrefix`或者`path`路径时，`unmatchedPath`的左边被逐渐“吃掉”。

在每个指令中，都由路径匹配DSL来精确地进行匹配并从中提取需要的内容。路径匹配DSL围绕着以下类型构建：

```scala
trait PathMatcher[L: Tuple]
type PathMatcher0 = PathMatcher[Unit]
type PathMatcher1[T] = PathMatcher[Tuple1[T]]
type PathMatcher2[T,U] = PathMatcher[Tuple2[T,U]]
// .. 等等
```

`PathMatcher`的实例对`unmatchedPath`进行精确的匹配和提取值，提取值的数量和类型由泛型参数`L`表示，`L`需要是Scala的`Tuple`或`Unit`类型之五（由`Tuple`上下文绑定限定）。

看一个较复杂的例子：

```Scala
val matcher: PathMatcher1[Option[Int]] =
  "foo" / "bar" / "X" ~ IntNumber.? / ("edit" | "create")
```

`matcher`将匹配`/foo/bar/X32/edit`或者`/foo/bar/X/create`。`"X" ~ IntNumber.?`的意思是匹配一个`X`字符加0个或多个十进制整数字符，同时所有整数字符全在一起数值取值范围为**int32**。

@@@ note { title='注意' }
路径匹配DSL在匹配URI路径以后才对已匹配部分进行解码。这意味着路径分隔符不能写到字符串中，必需使用`/`函数来定义，不然字符串`"foo/bar"`将匹配原始的字符串`"foo%2fbar"`，这应该不是你想要的。
@@@

#### 常用基本的`PathMatcher`

一个路径匹配器可以通过组合或修改多个基本的`PathMatcher`来构造，以下是常用的基本`PathMatcher`。

**字符串**

`String`的实例可以作为`PathMatcher0`。字符串只匹配自身，不提取任何值。需要注意的是字符串被解释为路径的解码表示（decode），当它包含一个'/'字符时，这个字符将与编码的原始URI中的'%2f'进行匹配。

**正则表达式**

`Regex`的实例可以作为`PathMatcher1[String]`。正则表达式不包含捕获组时将提取完整匹配或者只包含一个捕获组时提取捕获的内容，若正则表达式包含多个捕获组，则将引发`IllegalArgumentException`异常。

**`Segment: PathMatcher1[String]`**

从路径段不以'/'斜线开始匹配，将匹配的部分提取为字符串。

**`IntNumber: PathMatcher1[Int]`**

匹配一个或多个十进制字符为非负的`Int`数值。

**`LongNumber: PathMatcher1[Long]`**

匹配一个或多个十进制字符为非负的`Long`数值。

#### 组合子（Combinators）

Path的路径匹配器（Matcher）可以组合起来形成更高级的结构。

**`~` 操作符**

`~`操作符可以将两个Matcher指令连接起来。它将两个Matcher连接成一个，同时还保持Matcher的类型安全。如：`"foo" ~ "bar"` 等价于 `"foobar"`。

**`/` 操作符**

`/`操作符连接两个Matcher，并在中间插入 **Slash（`/`）** 匹配器。如：`"foo" / "bar"` 等价于 `"foo" ~ Slash ~ "bar"`。

**`|` 操作符**

`|`操作符组合了两个Matcher的方案，当且第一个不匹配时才尝试第二个，且两个Matcher必须具有兼容的类型。如：`("foo" | "bar") / "bom"`将先匹配`/foo/bom`路由，再匹配`/bar/bom`路由（'/'的优先级比'|'高，所以这里需要使用小括号括起来）。

#### 修饰符

'/'和'?'可作为修饰符作用于Matcher上，如下所示：

```scala
// 匹配 /foo/
path("foo"./)

// 匹配 /foo/bar
path("foo" / "bar")

// 注意: 匹配 /foo%2Fbar 而不是 /foo/bar
path("foo/bar")

// 匹配 /foo/bar
path(separateOnSlashes("foo/bar"))

// 匹配 /foo/123 等 并抽取 "123" 作为字符串类型的抽取值
path("foo" / """\d+""".r)

// 匹配 /foo/bar123 并抽取 "123" 作为字符串类型的抽取值
path("foo" / """bar(\d+)""".r)

// 类似 `path(Segments)`
path(Segment.repeat(10, separator = Slash))

// 匹配 /i42 等 或者 /hCAFE 等，并抽取值为Int类型
path("i" ~ IntNumber | "h" ~ HexIntNumber)

// 等价于 path("foo" ~ (PathEnd | Slash))
path("foo" ~ Slash.?)

// 匹配 /red 或 /green 或 /blue 并分别抽取 1, 2 或 3
path(Map("red" -> 1, "green" -> 2, "blue" -> 3))

// 匹配任何以 /foo 开头且不以 /foobar 开头的路径
pathPrefix("foo" ~ !"bar")
```

## MethodDirectives

HTTP方法指令用于匹配HTTP method，当请求不符合时将被拒绝，并返回 **405 Method Not Allowed**。常用预定义指令有：

- `get`：匹配HTTP的GET方法
- `post`：匹配HTTP的POST方法
- `put`：匹配HTTP的PUT方法
- `delete`：匹配HTTP的DELETE方法

### 抽取HttpMethod

除了在路由上匹配某个HTTP方法外，我们还可以抽取方法（HttpMethod），这个使用 `def extractMethod: Directive1[HttpMethod]`指令。示例如下：
```scala
val route =
  get {
    complete("This is a GET request.")
  } ~
    extractMethod { method =>
      complete(s"This ${method.name} request, clearly is not a GET!")
    }

// tests:
Get("/") ~> route ~> check {
  responseAs[String] shouldEqual "This is a GET request."
}

Put("/") ~> route ~> check {
  responseAs[String] shouldEqual "This PUT request, clearly is not a GET!"
}
Head("/") ~> route ~> check {
  responseAs[String] shouldEqual "This HEAD request, clearly is not a GET!"
}
```

### 自定义HttpMethod

当你需要使用非标准的Http方法时，可以通过`HttpMethod.custom`来自定义。
```scala
import akka.http.scaladsl.settings.{ ParserSettings, ServerSettings }

// 自定义HTTP方法:
val BOLT = HttpMethod.custom("BOLT", safe = false,
  idempotent = true, requestEntityAcceptance = Expected)

// 添加自定义方法到设置里
val parserSettings = ParserSettings(system).withCustomMethods(BOLT)
val serverSettings = ServerSettings(system).withParserSettings(parserSettings)

val routes = extractMethod { method ⇒
  complete(s"This is a ${method.name} method request.")
}
val binding = Http().bindAndHandle(routes, host, port, settings = serverSettings)

val request = HttpRequest(BOLT, s"http://$host:$port/", protocol = `HTTP/1.1`)
```

### overrideMethodWithParameter

```scala
def overrideMethodWithParameter(paramName: String): Directive0
```

`overrideMethodWithParameter`指令可以通过指定的请求参数来改变`HttpRequest`保存的请求HTTP方法。这在一些遗留项目或特殊环境下有用。比如：某些网络环境不支持`DELETE`、`PUT`方法，就可以通过如下的方式将一个带特定参数的请求转换成对应的`DELETE`或`PUT`请求。
```scala
val route =
  overrideMethodWithParameter("method") {
    get {
      complete("This looks like a GET request.")
    } ~
      post {
        complete("This looks like a POST request.")
      }
  }

// tests:
Get("/?method=POST") ~> route ~> check {
  responseAs[String] shouldEqual "This looks like a POST request."
}
Post("/?method=get") ~> route ~> check {
  responseAs[String] shouldEqual "This looks like a GET request."
}

Get("/?method=hallo") ~> route ~> check {
  status shouldEqual StatusCodes.NotImplemented
}
```

## ParameterDirectives，FormFieldDirectives

TODO

## MarshallingDirectives

TODO

## FileUploadDirectives

TODO

## CookieDirectives

TODO
