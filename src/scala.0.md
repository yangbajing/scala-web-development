# Scala 语言基础

**REPL**

有两种启动 **REPL** 的方式，一种是直接运行 `scala` 命令：

```
cd $SCALA_HOME
./bin/scala
```

还有种是在 Sbt工程登录进入 **Sbt Console**：

```
sbt console
```

或者在进入 Sbt 交互式控制台后再执行 `console` 命令。

启动Scala REPL，它是一个基于命令行的交互式编程环境。对于有着Python、Ruby等动态语言的同学来说，这是一个很常用和工具。但Javaer
们第一次见到会觉得比较神奇。我们可以在REPL中做一些代码尝试而不用启动笨拙的IDE，这在我们思考问题时非常的方便。对于Javaer有一个好消息，JDK 9开
始将内建支持REPL功能。

```
sbt:scala-seed> console
[info] Starting scala interpreter...
Welcome to Scala 2.12.4 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_151).
Type in expressions for evaluation. Or try :help.

scala> 
```

接下来我们将在 Scala REPL 中测试本章的示例代码。
