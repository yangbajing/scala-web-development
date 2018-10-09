# 实战：为Ant Design Pro提供后端接口

之前章节已经了解了Akka HTTP的路由定制、数据序列化等内容，是时候开始一个比较完整的Web应用示例了。这里我们将使用 Akka HTTP 来集成 **Ant Design Pro** ，Ant Design Pro是一个开箱即用的中台前端/设计解决方案，它由蚂蚁金服开发，官方地址：[https://pro.ant.design/index-cn](https://pro.ant.design/index-cn)。

本文假定用户已经熟悉并会使用 Ant Design Pro，若还未接触过可以从官方文档开始：[https://pro.ant.design/docs/getting-started-cn](https://pro.ant.design/docs/getting-started-cn)。

*本文使用 Ant Design Pro 2.0版本*

## 设置 Ant Design Pro

Ant Design Pro 已经是一个完整的后台前端应用，我们只需要使用 Akka HTTP 为其提供后端API接口服务支持和静态资源文件的HTTP获取功能。

### 创建API

Akka HTTP 的 Routing DSL 是从上到下一级一级的匹配路由的，当前一个路由不匹配时才判断下一个路由，这样一直到最后一个。利用这个特性，我们可以在整个路由定义的最后来设置返回React SPA需要的静态资源文件。

@@snip [Routes.scala](../../../../../ant-design-pro/src/main/scala/scalaweb/ant/design/pro/route/Routes.scala) { #routes }

这里的重点在 `notPathPrefixTest(api) { .... }` 部分，这一块代码是用来返回 Ant Design Pro 静态资源的。首先它将判断请求URI不是以 `/api` 开头，若请求URI以`/api`开关则不进入里面的获取静态资源代码逻辑，而是直接返回一个预定义的指令：`reject`。通常，我们都会将API接口统一到 `/api` 这样的路径下，这样非 `/api` 开头的URI请求就可以交到下面的两句代码执行，来实现SPA应用在资源未找到时服务端默认返回 `/index.html` 的需求。

```
getFromResourceDirectory("dist") ~
  getFromResource("dist/index.html")
```

- `getFromResourceDirectory`：根据URI请求路径从资源目录dist查找文件并返回
- `getFromResource`：直接返回 dist/index.html 资源文件

总体上，以上两个指令组合使用就可以实现类似 Nginx 的 `try_files $uri /index.html;` 效果

**Mocks.scala**，定义了API接口数据并组装成 `HttpEntity` 对象。

@@snip [Routes.scala](../../../../../ant-design-pro/src/main/scala/scalaweb/ant/design/pro/mock/Mocks.scala) { #dashboard-mock-api }

这里为了演示Akka HTTP与Ant Design Pro的集成，我并未直接去实现后端接口数据模型的生成逻辑，比如：model定义、数据存储操作等。而是通过直接返回字符串形式的JSON数据来模拟：

@@snip [Routes.scala](../../../../../ant-design-pro/src/main/scala/scalaweb/ant/design/pro/mock/Api.scala) { #currentUser }

通过`def toJsonEntity(str: String): HttpEntity.Strict = HttpEntity(ContentTypes.`application/json`, str)`函数，将JSON字符串实例化为一个`HttpEntity`对象并设置`Content-Type`为`application/json`类型。

@@@ note { title='注意' }
这里只定义了Ant Design Pro的 **dashboard** 栏目下3个页面需要的接口，其它接口并未实现，因为对于这个示例它们并不是重点。
@@@

### 添加webpack proxy支持

修改 `ant-design-pro/web/config/config.js` 文件，在末尾右大括号（`}`）上方添加 `proxy` 设置API代理访问路径路径。这样所有的前端Ajax请求（请求`/api`开始的路径）都会被路由到Akka HTTP提供的API服务上。

```javascript
  proxy: {
    '/api': {
      target: 'http://localhost:22222',
      changeOrigin: true,
    },
  },
```

使用 `start:no-mock` 启动Ant Design Pro
```
npm run start:no-mock
```

打开浏览器，访问 [http://localhost:8000](http://localhost:8000) 地址：

![](.../ant-design-pro-500.png)

这时还未启动Akka HTTP后端服务，看到在请求后端API `/api/currentUser`时报504网关超时错误。这代表我们设置的 webpack.proxy 已经生效，接下来让我们启动Akka HTTP后端服务。

### 启动Akka HTTP Server

**Main.scala**

@@snip [Routes.scala](../../../../../ant-design-pro/src/main/scala/scalaweb/ant/design/pro/Main.scala) { #main }

可以看到，编程启动Akka HTTP服务非常简单。我们执行`Main.scala`即可启动Akka HTTP服务。看到类似输出就代表服务已经启动成功：

```
10:59:13.659 [default-akka.actor.default-dispatcher-4] INFO scalaweb.ant.design.pro.Main$ - 启动Akka HTTP Server成功，绑定地址: ServerBinding(/0:0:0:0:0:0:0:0:22222)
```

## 打包、部署

Ant Design Pro 的 Akka HTTP集成已经完成，我们也在开发模式下分别启动了Webpack Dev Server和Akka HTTP Server来看到我们集成的效果。要把集成的成果部署到服务器上怎么办？非常的简单，执行如下的几行命令就可以生成一个同时提供后端API接口和HTTP静态资源渲染的独立可执行jar包。不需要使用Nginx/Apache的代理静态资源，这样部署更加简洁。当然，你也可以继续使用用Nginx/Apache来代理静态资源，如果需要的话。

```
pushd ant-design-pro/web
yarn install
yarn run build
popd
rm -rf ant-design-pro/src/main/resources/dist/*
cp ant-design-pro/web/dist/* ant-design-pro/src/main/resources/dist/
sbt "project ant-design-pro" assembly
```

1. 首先编译 Ant Design Pro，在 `dist` 目录生成静态资源。
2. copy所有静态资源到 `resources/dist` 目录，这样Akka HTTP可以在生成的jar里通过Java资源文件机制访问到它们。
3. 使用 `sbt assembly` 命令打包。
4. 使用 `java -jar` 命令执行可执行jar包文件。

**运行程序**

```
java -jar ant-design-pro/target/scala-2.12/ant-design-pro-assembly-1.0.0.jar
```

打开浏览器访问 [http://localhost:22222/](http://localhost:22222/) 即可看到 Ant Design Pro 的界面。

*示例效果*
![](.../account-center-01.png)

## 总结

我们通过一个简单的实战示例：ant-desigin-pro，将之前几章所讲知识串起来通过Akka HTTP技术实现了一个较为完整的Web应用。

*本章源码在：[https://github.com/yangbajing/scala-web-development/tree/master/ant-design-pro](https://github.com/yangbajing/scala-web-development/tree/master/ant-design-pro)*
