# Kryo

Kryo是一种快速高效的用于Java的二进制对象图序列化框架。具有调整、压缩率高和易于使用的特性。

在Akka中使用Kryo可以采用Twitter开源的 [chill](https://github.com/twitter/chill) 库，它可以简化对Kryo的使用并对Scala有着更友好的API。

在Akka里使用`chill`非常的简单，首先需要引入相关库依赖：
```scala
libraryDependencies += "com.twitter" %% "chill-akka" % "0.9.3"
```

在修改Akka配置：
```
akka.actor {
  # 启用附加的自定义序列化绑定功能
  enable-additional-serialization-bindings = on

  # 禁用Java默认序列化功能
  allow-java-serialization = off

  serializers {
    kryo = "com.twitter.chill.akka.AkkaSerializer"
  }

  serialization-bindings {
    # 指定所有实现了`java.io.Serializable`接口的类都使用 kryo 序列化
    "java.io.Serializable" = kryo
  }
}
```
