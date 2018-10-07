# OAuth 2接口设计

根据上一节的介绍，我们这里只实现部分 OAuth 2的功能，完整的OAuth 2功能实现留待作者自行实现。

首先我们来实现 OAuth 2 的接口，OAuth 2为基于HTTP的协议，首先来设计并实现 OAuth 2 的API接口。

## OAuth 2 API接口

实现一个OAuth 2服务端功能，需要定义如下几个接口：

``` scala
  override def route: Route = pathPrefix("oauth2") {
    authorizeSigninHTML ~
      signinRoute ~
      tokenGetRoute ~
      validationRoute
  }
```

- **`/oauth2/authorize`  authorizeSigninHTML**：返回OAuth 2登录页面
- **`/oauth2/signin`     signinRoute**：OAuth 2用户登录请求接口
- **`/oauth2/token`      tokenGetRoute**：通过code获取`access_token`接口
- **`/oauth2/validation` validationRoute**：校验`access_token`是否有效

### OAuth 2登录页面

```scala
  def authorizeSigninHTML: Route = pathGet("authorize") {
    getFromResource("html/oauth2/authorize.html")
  }
```

`getFromResource`通过从Java类查找路径（classpath）中找到指定的文件并返回给客户端，同时会通过文件后缀名来设置响应的`Content-Type`。

### OAuth 2用户登录请求

```scala
  def signinRoute: Route = pathPost("signin") {
    val pdm = ('account, 'password, 'response_type, 'client_id, 'redirect_uri, 'scope, 'state)
    formFields(pdm).as(AuthorizeSigninRequest.apply _) { req =>
      onSuccess(oauth2Service.authorizeSignin(req)) { redirectUri =>
        complete(HttpResponse(StatusCodes.Found, headers = List(Location(redirectUri))))
      }
    }
  }
```

用户登录需要使用`application/x-www-form-urlencoded`请求类型发送登录信息，登录成功的话将响应HTTP状态码302，并重定向到登录成功后要访问的接入应用页面。

*这里需要使用传统的 <form action="post">....</form> 方式来提交登录请求，这样响应里的 'Location' 重定向才能生效。*

### 通过`code`获取`access_token`

``` scala
  def tokenGetRoute: Route = (path("token") | path("access_token") & get) {
    val tokenRequestPDM = ('grant_type, 'client_id, 'client_key, 'code, 'redirect_uri, 'echostr)
    parameters(tokenRequestPDM).as(AuthorizeTokenRequest.apply _) { req =>
      extractExecutionContext { implicit ec =>
        val future = req.grantType match {
          case OAuthConstants.AUTHORIZATION_CODE => oauth2Service.accessTokenForAuthorization(req)
          case OAuthConstants.CLIENT_CREDENTIALS => oauth2Service.accessTokenForClient(req)
        }
        futureComplete(future)
      }
    }
  }
```

在用户通过OAuth 2登录成功后，服务端会在`redirect_uri`回调地址带上`code`参数来访问应用的一个登录成功回调地址，应用在获取到`code`参数后请通过调用此接口来获取访问令牌：`access_token`。这里需要传6个参数：

- `grant_type`：授权类型，这里我们实现了对 **authorization\_code** 类型的支持。
- `client_id`：应用客户端ID。
- `client_key`：应用客户端密钥。
- `code`：获取到的code
- `redirect_uri``：重定向页面，与之前通过OAuth 2登录页面成功后重定向的地址相同。
- `echostr`：随机字符串

### 校验 `access_token` 是否有效

```
  def validationRoute: Route = pathGet("validation") {
    optionalAccessToken {
      case Some(accessToken) =>
        onSuccess(oauth2Service.validationAccessToken(accessToken)) {
          case true => complete(StatusCodes.OK)
          case _    => complete(StatusCodes.Unauthorized)
        }
      case _ => reject(OAuthRejection("参数'access_token'缺失"))
    }
  }
```

此接口用于校验 `access_token` 是否有效，若校验有效的话将返回预计到期时间（epoch seconds）。


这里我们使用了一个自定义指令 `optionalAccessToken` 来从请求中获取 `access_token`，代码如下：

```scala
  def optionalAccessToken: Directive1[Option[String]] = extract { ctx =>
    ctx.request.header[Authorization].flatMap { header =>
      header.credentials match {
        case OAuth2BearerToken(accessToken) => Some(accessToken)
        case _                              => None
      }
    } orElse
      ctx.request.uri.query().get("access_token") orElse
      ctx.request.headers.find(_.lowercaseName() == "access_token").map(_.value())
  }
```

在OAuth 2官方协议里，`access_token`可以通过多种方式进行传输：

1. Authorization头设置 `bearer` 方式
2. 通过form表单方式
3. 通过url参数

这里，为了HTTP请求方法（method）的统一，实现为分别从`Authorization` header头、uri请求参数、命名为`access_token`的 header头三种形式依次获取。

