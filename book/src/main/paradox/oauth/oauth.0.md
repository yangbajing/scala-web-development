# 实战：OAuth 2 服务

这章进行一个稍微复杂一点的实战项目：实现 OAuth 2 服务。

## OAuth 2是如何工作的

OAuth 2 在整个流程中有四种角色:

* 资源拥有者(Resource Owner) - 这里是Tom
* 资源服务器(Resource Server) - 这里是Facebook
* 授权服务器(Authorization Server) - 这里当然还是Facebook，因为Facebook有相关数据
* 客户端(Client) - 这里是某App

当Tom试图登录Facebook，某App将他重定向到Facebook的授权服务器，当Tom登录成功，并且许可自己的Email和个人信息被某App获取。这两个资源被定义成一个Scope（权限范围），一旦准许，某App的开发者就可以申请访问权限范围中定义的这两个资源。

```markdown
+--------+                               +---------------+
|        |--(A)- Authorization Request ->|   Resource    |
|        |                               |     Owner     |
|        |<-(B)-- Authorization Grant ---|               |
|        |                               +---------------+
|        |
|        |                               +---------------+
|        |--(C)-- Authorization Grant -->| Authorization |
| Client |                               |     Server    |
|        |<-(D)----- Access Token -------|               |
|        |                               +---------------+
|        |
|        |                               +---------------+
|        |--(E)----- Access Token ------>|    Resource   |
|        |                               |     Server    |
|        |<-(F)--- Protected Resource ---|               |
+--------+                               +---------------+
```

## 为什么是JWT

OAuth 2并不关心去哪找Access Token和把它存在什么地方的，生成随机字符串并保存Token相关的数据到这些字符串中保存好。通过一个令牌端点，其他服务可能会关心这个Token是否有效，它可以通过哪些权限。这就是用户信息URL方法，授权服务器为了获取用户信息转换为资源服务器。

当我们谈及微服务时，我们需要找一个Token存储的方式，来保证授权服务器可以被水平扩展，尽管这是一个很复杂的任务。所有访问微服务资源的请求都在Http Header中携带Token，被访问的服务接下来再去请求授权服务器验证Token的有效性，目前这种方式，我们需要两次或者更多次的请求，但这是为了安全性也没什么其他办法。但扩展Token存储会很大影响我们系统的可扩展性，这是我们引入JWT（读jot）的原因。

```markdown
+-----------+                                     +-------------+
|           |       1-Request Authorization       |             |
|           |------------------------------------>|             |
|           |     grant_type&username&password    |             |--+
|           |                                     |Authorization|  | 2-Gen
|  Client   |                                     |Service      |  |   JWT
|           |       3-Response Authorization      |             |<-+
|           |<------------------------------------| Private Key |
|           |    access_token / refresh_token     |             |
|           |    token_type / expire_in / jti     |             |
+-----------+                                     +-------------+
```