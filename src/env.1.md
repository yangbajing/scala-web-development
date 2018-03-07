---
description: 安装Sbt，并创建第一个sbt工程
---


# Sbt

Scala有一套官方的编译、构建工具 Sbt，全称：The interactive build tool （交互构建工具）。它除了能做向 Maven、Gradle类似的
事情以外，它还有一个交互式的环境，可以在里面执行一些常用命令，甚至直接执行、测试代码。

## 安装 Sbt

*详细安装教程请阅读官方文档：[https://www.scala-sbt.org/1.x/docs/index.html](https://www.scala-sbt.org/1.x/docs/index.html)*

**Linux/Unix/Mac**

下载安装并配置Sbt：

```
wget -c http://file.helloscala.com/sbt-1.1.4.tgz
tar zxf sbt-1.1.1.tgz
echo 'export SBT_HOME="/home/yangjing/sbt-1.1.4"
export PATH="$SBT_HOME/bin:$PATH"
' >> ~/.bash_profile
. ~/.bash_profile
```

现在可以在当前终端执行 `sbt` 命令了，若在重启系统前需要在其它终端也运行 `sbt` 命令，需要执行 `. ~/.bash_profile` 以使环
境变量生效。

**Windows**

下载 [sbt-1.1.1.msi](http://file.helloscala.com/sbt-1.1.1.msi) ，双击安装。

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

![runMain example.Hello](imgs/01.1.runMain.png)

*运行 scalatest 测试*

![testOnly example.HelloSpec](imgs/01.1.testOnly.png)
