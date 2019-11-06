# 常用指令（Directives）

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

val routes = extractMethod { method =>
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
