# Sbt


Scala有一套官方的编译、构建工具 Sbt，全称：The interactive build tool （交互构建工具）。它除了能做向 Maven、Gradle类似的
事情以外，它还有一个交互式的环境，可以在里面执行一些常用命令，甚至直接执行、测试代码。

## 安装 Sbt

*详细安装教程请阅读官方文档：[https://www.scala-sbt.org/1.x/docs/index.html](https://www.scala-sbt.org/1.x/docs/index.html)*

**Linux/Unix/Mac**

下载安装并配置Sbt：

```
wget -c https://github.com/sbt/sbt/releases/download/v1.2.8/sbt-1.2.8.tgz
tar zxf sbt-1.2.8.tgz
echo 'export SBT_HOME="/home/yangjing/sbt-1.2.8"
export PATH="$SBT_HOME/bin:$PATH"
' >> ~/.bash_profile
. ~/.bash_profile
```

现在可以在当前终端执行 `sbt` 命令了，若在重启系统前需要在其它终端也运行 `sbt` 命令，需要执行 `. ~/.bash_profile` 以使环
境变量生效。

**Windows**

下载 [https://github.com/sbt/sbt/releases/download/v1.2.8/sbt-1.2.8.msi](https://github.com/sbt/sbt/releases/download/v1.2.8/sbt-1.2.8.msi) ，双击安装。

## 创建一个 Sbt 项目

创建一个 Sbt 还是比较简单的，你可以从使用 `sbt new` 命令从线上众多的模板开始，也可以手动创建。

**`sbt new`**

```bash
sbt new scala/scala-seed.g8

Minimum Scala build. 

name [My Something Project]: scala-seed

```

当前目录下多了个 `scala-seed` 目录，通过 `sbt new` 创建的项目里面的 Scala 版本可能不是你想要的。不过在这里不用担心，保持默认即可。（本书
将基于 Scala 2.12.x 讲解）。

**Sbt 控制台**

Sbt 是一个交互式的构建工具，它具有一个 REPL 的命令行控制台，你可以在里面编译、运行、测试，甚至打包及执行自定义命令……

```
[yangjing@yangjing-31 scala-seed-project]$ /usr/bin/sbt
[info] Loading settings from idea.sbt ...
[info] Loading global plugins from /opt/local/share/sbt/1.0/plugins
[info] Loading project definition from /tmp/scala-seed-project/project
[info] Loading settings from build.sbt ...
[info] Set current project to Scala Seed Project (in build file:/tmp/scala-seed-project/)
[info] sbt server started at local:///home/yangjing/.sbt/1.0/server/d42fb1897d8aa6920ae0/sock
sbt:Scala Seed Project>
```

*运行示例程序*

![runMain example.Hello](.../01.1.runMain.png)

*运行 scalatest 测试*

![testOnly example.HelloSpec](.../01.1.testOnly.png)

## 多项目工程

通常在真实的应用开发中，我们都会在一个工程里有多个子项目，使用子项目的形式来区分不同模块。我们只需要在
[`build.sbt`](../../../../../codes/multi-project/build.sbt) 工程配置文件中使用 `project` 指令来定义不同的子项目即可，一个示例如下：

@@snip [build.sbt](../../../../../codes/multi-project/build.sbt)

- **project**：需要传入一个`File`参数，用于设置子项目的本地路径。
- **aggregate**：根项目中用于聚合需要的子项目。在Sbt控制台不进入子项目的情况下使用`compile`、`test`等命令时会依次进入所有配置了的子项目执行命令。
- **dependsOn**：指定依赖的项目。
- **settings**：对应单项目配置时的配置项都在此设置。

多项目工程目录结构如下：
```
├── app
│   ├── src
│   │   ├── main
│   │   └── test
├── common
│   ├── src
│   │   ├── main
│   │   └── test
├── project
│   ├── build.properties
├── build.sbt
```

@@@note { title=注意 }
默认，sbt不会创建`src/main/scala`、`src/test/scala`等相关源码目录，这需要我们手动创建。
@@@