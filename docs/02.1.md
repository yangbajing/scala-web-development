# 你好，Scala

Scala 是一门表达力很强的语言，它可以成为你工作中优秀的助手。不免俗，我们来看看 Scala 里的 helloworld：

```scala
println("你好，Scala！")
```

将代码保存为 `helloworld.scala` 文件，在命令行使用以下命令直接运行（你也可以在 **REPL** 中直接输入上面代码运行）。：

```
$ scala helloworld.scala
$ 你好，Scala！
```

Scala 可以直接执行代码文件，这个特性可以让我们使用 Scala 来写系统脚本（虽然实践中我还没这么做，因为Scala的编译、启动还是比较慢的）。我们可以
看到，代码中使用了 `println` 函数来输出字符串，这其实是对 `java.lang.System.out.println` 的封装。
