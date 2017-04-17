# 并发

Scala是对多核和并发编程的支付做得非常好，它的Future类型提供了执行异步操作的高级封装。

Future对象完成构建工作以后，控制权便会立刻返还给调用者，这时结果还不可以立刻可用。Future实例是一个句柄，它指向最终可用的结果值。不论操作成功与否，在future操作执行完成前，代码都可以继续执行而不被阻塞。Scala提供了多种方法用于处理future。

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

val futures = (0 until 10).map { i =>
  Future {
    val s = i.toString
    print(s)
    s
  }
}

val future = Future.reduce(futures)((x, y) => x + y)

val result = Await.result(future, Duration.Inf)

// Exiting paste mode, now interpreting.

0132564789

scala> val result = Await.result(future, Duration.Inf)
result: String = 0123456789
```

上面代码创建了10个`Future`对象，`Future.apply`方法有两个参数列表。第一个参数列表包含一个需要并发执行的命名方法体（by-name body）；而第二个参数列表包含了隐式的`ExecutionContext`对象，可以简单的把它看作一个线程池对象，它决定了这个任务将在哪个异步（线程）执行器中执行。`futures`对象的类型为`IndexedSeq[Future[String]]`。本示例中使用`Future.reduce`把一个`futures`的`IndexedSeq[Future[String]]`类型压缩成单独的`Future[String]`类型对象。`Await.result`用来阻塞代码并获取结果，输入的`Duration.Inf`用于设置超时时间，这里是无限制。

这里可以看到，在`Future`代码内部的`println`语句打印输出是无序的，但最终获取的`result`结果却是有序的。这是因为虽然每个`Future`都是在线程中无序执行，但`Future.reduce`方法将按传入的序列顺序合并结果。

除了使用`Await.result`阻塞代码获取结果，我们还可以使用事件回调的方式异步获取结果。`Future`对象提供了几个方法通过回调将执行的结果返还给调用者，常用的有：

1. onComplete: PartialFunction[Try[T], Unit]：当任务执行完成后调用，无论成功还是失败
2. onSuccess: PartialFunction[T, Unit]：当任务成功执行完成后调用
3. onFailure: PartialFunction[Throwable, Unit]：当任务执行失败（异常）时调用

```scala
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

val futures = (1 to 2) map {
  case 1 => Future.successful("1是奇数")
  case 2 => Future.failed(new RuntimeException("2不是奇数"))
}

futures.foreach(_.onComplete {
  case Success(i) => println(i)
  case Failure(t) => println(t)
})

Thread.sleep(2000)
```

`futures.onComplete`方法是一个偏函数，它的参数是：`Try[String]`。`Try`有两个子类，成功是返回`Success[String]`，失败时返回`Failure[Throwable]`，可以通过模式匹配的方式获取这个结果。