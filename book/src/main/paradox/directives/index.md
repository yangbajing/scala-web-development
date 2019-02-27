# 常用指令

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

@@toc { depth=3 }

@@@ index

* [PathDirectives（路径指令）](path.md)
* [MethodDirectives](method.md)
* [ParameterDirectives，FormFieldDirectives](parameter_form.md)
* [MarshallingDirectives](marshalling.md)
* [FileUploadDirectives](file.md)
* [CookieDirectives](cookie.md)

@@@
