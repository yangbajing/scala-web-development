# 部署

## sbt-assembly 

### 构建

使用`sbt-assembly`可以把程序打包成一个单一的jar包发布，需要在sbt插件配置（`project/plugins.sbt`）添加发下插件：

```sbt
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")
```

然后在`sbt`项目的`settings`中添加如下设置：

```sbt
mainClass in assembly := Some("greeter.GreeterApplication"),
test in assembly := {},
assemblyMergeStrategy in assembly := {
  case PathList("io", "netty", xs @ _*)               => MergeStrategy.first
  case PathList("google", "protobuf", xs @ _*)        => MergeStrategy.first
  case PathList("com", "google", "protobuf", xs @ _*) => MergeStrategy.first
  case PathList("scalapb", xs @ _*)                   => MergeStrategy.first
  case "application.conf"                             => MergeStrategy.concat
  case "reference.conf"                               => MergeStrategy.concat
  case "module-info.class"                            => MergeStrategy.concat
  case "META-INF/io.netty.versions.properties"        => MergeStrategy.first
  case "META-INF/native/libnetty-transport-native-epoll.so" =>
    MergeStrategy.first
  case n if n.endsWith(".txt")   => MergeStrategy.concat
  case n if n.endsWith("NOTICE") => MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
```

`mainClass`指定当通过`java -jar assembly.jar`命令运行jar包时，默认的启动类（启动类必须有`main`函数）。

因为是将所有源码和依赖打到一个jar包，所以需要配置`assemblyMergeStrategy`来决定当文件名起冲突时的合并策略。

**assembly**

通过在sbt shell执行`assembly`命令，即可生成可运行的jar包。

```sbtshell
> grpc/assembly
[info] Strategy 'concat' was applied to 2 files (Run the task at debug level to see details)
[info] Strategy 'deduplicate' was applied to 667 files (Run the task at debug level to see details)
[info] Strategy 'discard' was applied to 89 files (Run the task at debug level to see details)
[info] Strategy 'filterDistinctLines' was applied to 2 files (Run the task at debug level to see details)
[info] Strategy 'first' was applied to 337 files (Run the task at debug level to see details)
[info] Assembly up to date: /home/yangjing/workspace/scala-web-development/grpc/target/scala-2.13/grpc-assembly-1.0.0.jar
[success] Total time: 3 s, completed Nov 24, 2019 6:24:09 PM
```

### 运行

通过`java -jar`命令运行`gprc-assembly-1.0.0.jar`时，需要提供 `jetty-alpn-agent` **Agent**，可以在此下载 `jetty-alpn-agent`：

```shell script
wget -c https://repo1.maven.org/maven2/org/mortbay/jetty/alpn/jetty-alpn-agent/2.0.9/jetty-alpn-agent-2.0.9.jar
```

即可使用如下命令启动gRPC服务：

```shell script
java -javaagent:jetty-alpn-agent-2.0.9.jar -jar grpc-assembly-1.0.0.jar
```

看到类似如下输出，则代表Greeter gRPC服务已启动：
```
18:37:04.221 [grpc-akka.actor.default-dispatcher-3] INFO akka.event.slf4j.Slf4jLogger - Slf4jLogger started
18:37:05.093 [grpc-akka.actor.default-dispatcher-5] INFO greeter.GreeterApplication$ - Greeter gRPC server started, bind to ServerBinding(/127.0.0.1:8000).
```

## sbt-native-packager

### 配置

需要添加`sbt-native-packager` sbt插件并启动`JavaAppPackaging`插件：

**project/plugins.sbt**

```sbt
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.4.1")
```

**sbt项目配置**

```sbt
enablePlugins(JavaAppPackaging)
```

*`mainClass`设置选项在 sbt-native-packager 里也是需要的，它指定了程序运行时执行的主类。*

使用 sbt-native-packager 进行程序打包，运行时不需要再手动指定 `-javaagent`，它会在生成的启动脚本里根据sbt的`javaAgents`配置项设置后相应的启动命令行参数。 

### 打包

#### dist

使用`dist`命令即可在sbt shell类打包：

```sbtshell
> grpc/dist
....
[success] All package validations passed
[info] Your package is ready in /home/yangjing/workspace/scala-web-development/grpc/target/universal/grpc-1.0.0.zip
[success] Total time: 12 s, completed Nov 24, 2019 6:45:55 PM
```

它将生成一个zip压缩包`grpc-1.0.0.zip`在项目的`target/universal`目录，将压缩包上传到服务器上脚本后执行`bin`目录里的启动脚本（启动脚本的名字默认为项目名）即可运行程序了。

#### stage

你也可以执行`stage`命令生成完整的包文件路径，而不是生成一个压缩包。`stage`命令运行后文件路径为：`target/universal/stage`。
```sbtshell
> grpc/stage
```

**stage** 目录结构类似：
```
├── bin
│   ├── grpc
│   └── grpc.bat
├── jetty-alpn-agent
│   └── jetty-alpn-agent-2.0.9.jar
└── lib
    ├── ch.qos.logback.logback-classic-1.2.3.jar
    ├── ch.qos.logback.logback-core-1.2.3.jar
    ├── com.fasterxml.jackson.core.jackson-annotations-2.10.0.jar
    ├── ....
```
