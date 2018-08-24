# OAuth 2简介

OAuth 2 是OAuth协议的延续版本，但不向后兼容OAuth 1.0即完全废止了OAuth 1.0。 OAuth 2关注客户端开发者的简易性。
要么通过组织在资源拥有者和HTTP服务商之间的被批准的交互动作代表用户，要么允许第三方应用代表用户获得访问的权限。
同时为Web应用，桌面应用和手机，和起居室设备提供专门的认证流程。2012年10月，OAuth 2协议正式发布为 @extref[RFC 6749](rfc:6749)。

##  OAuth 2 模式

Web ServerFlow是把OAuth 1.0的三个步骤缩略为两个步骤，首先这个是适合有server的第三方使用的。

1. 客户端通过HTTP请求Authorize
2. 服务端接收到Authorize请求，返回用户登陆页面
3. 用户在登陆页面登陆
4. 登录成功后，服务端将浏览器定位到 `redirect_uri`，并同时传递Authorization Code
5. 客户端使用HTTPS发送Authorization Code
6. 服务器端收到 `access_token` 请求，验证Authorization Code——生成 `access_token`，`refresh_token`和`expires_in`（过期时间）——`access_token`和`refresh_token`和过期时间入库
7. 返回`access_token`和`refresh_token`，`expires_in`（过期时间）
8. 用户使用HTTPS协议，发送`access_token`及相应参数请求开放平台接口


