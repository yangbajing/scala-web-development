# Scalatest

ScalaTest通过简单、清晰的测试和可执行的规范来提高团队的生产力，同时改进代码和沟通效率。

ScalaTest是Scala生态系统中最灵活、最流行的测试工具。支持测试：Scala、Scala.js（Javascript）和Java代码。可与JUnit、TestNG、Ant、Maven、sbt、ScalaCheck、JMock、EasyMock、Mockito、ScalaMock、Selenium、Eclipse、Netbeans、Intellij、VSCode等工具集成使用。ScalaTest可使Scala、Scala.js或者Java项目的测试更容易，拥有更高的生产力水平。

为了最大化生产力，ScalaTest内建扩展点并支持多种测试方式。我们可以选择最适合我们团队经验和文化的测试风格。有以下风格可供选择：

- FunSuite：来自xUnit。
- FlatSpec：另一个来自xUnit。
- FunSpec：来自Ruby's RSpec的BDD测试风格。
- WordSpec：来自specs、specs2，适合训练有素的团队来定义严格的测试规范。也是Akka、Playframework等推荐的风格。
- FreeSpec：适合有经验的团队。
- PropSpec：适合追求完美的团队，需要前置测试条件定义。
- FeatureSpec：主要用于验收测试。
- RefSpec（JVM only）：需要定义一个特殊的测试函数，通过测试函数字面量来代码测试功能。

## 安装 ScalaTest

ScalaTest的安装、使用很简单，可以直接在命令行使用，如：

```
$ scalac -cp scalatest-app_2.12-3.0.5.jar ExampleSpec.scala
```

也可以和sbt集成使用。对sbt不了解的读者可以先看：[http://www.yangbajing.me/scala-web-development/env.1.html](http://www.yangbajing.me/scala-web-development/env.1.html) 来快速的学习sbt的使用方法。

在sbt中添加ScalaTest支持非常简单，在构建配置文件（一般是build.sbt）中添加库依赖即可。

```
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
```

之后重启sbt或在sbt命令行控制台里输入：`reload`以使其生效。

## 第一个测试用例

```
import scala.collection.mutable
import org.scalatest._

class FirstTest extends WordSpec with Matchers {
  "A Stack" should {
    "pop values in last-in-first-out order" in {
      val stack = mutable.Stack.empty[Int]
      stack.push(1)
      stack.push(2)
      stack.pop() shouldBe 2
      stack.pop() shouldBe 1
    }
    
    "throw NoSuchElementException if an empty stack is popped" in {
      val emptyStack = mutable.Stack.empty[Int]
      assertThrows[NoSuchElementException] {
        emptyStack.pop()
      }
    }
  }
}
```

运行测试有两种方式：

1. 使用`test`命令运行所有测试
2. 使用`testOnly`命令运行单个测试。

在sbt中输入`testOnly firstFirstTest`运行刚写好的第一个测试，结果如下：

```
[IJ]scalatest > testOnly first.FirstTest
[info] Compiling 1 Scala source to /opt/workspace/scala-applications/scalatest/target/scala-2.12/test-classes ...
[info] Done compiling.
[info] FirstTest:
[info] A Stack
[info] - should pop values in last-in-first-out order
[info] - should throw NoSuchElementException if an empty stack is popped
[info] Run completed in 344 milliseconds.
[info] Total number of tests run: 2
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 2, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 2 s, completed 2018-8-2 18:29:14
```

## 使用 Matchers

除了默认的断言函数，如：`assert`、`assertResult`、`assertThrows`等，ScalaTest还提供了更好用的 **Matchers**。**Matchers** 具有以下特性：

- 基于表达式断言的DSL，如：`stack.pop() shouldBe 2`。更易读，以人类语言的方式来编写测试断言。
- 丰富的断言类型，支持更直观的断言表达式，如：`"abbccxxx" should startWith regex ("a(b*)(c*)" withGroups ("bb", "cc"))`。

只需要在测试类混入 `Matchers` 特质，就可以使用 ScalaTest 提供的强大的 Matchers 特性。

## OptionValues

OptionValues特质提供了一个隐式转换，将一个 `value` 方法添加到 `Option[T]` 类型上。若 Option 是有定义的，则 `value` 方法将返回值，就和调用 `.get` 一样；若没有，则抛出 `TestFailedException` 异常，而不是调用 `get` 方法时抛出的 `NoSuchElementException` 异常。同时，ScalaTest会输出更友好的错误显示：**The Option on which value was invoked was not defined.**，而不是输出一大堆的错误异常栈而打乱正常的测试输出。

**使用`.value`**

```
[info] FirstTest:
[info] option
[info] - should value *** FAILED ***
[info]   The Option on which value was invoked was not defined. (FirstTest.scala:30)
[info] Run completed in 418 milliseconds.
[info] Total number of tests run: 3
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 2, failed 1, canceled 0, ignored 0, pending 0
[info] *** 1 TEST FAILED ***
[error] Failed tests:
[error] 	first.FirstTest
[error] (Test / testOnly) sbt.TestsFailedException: Tests unsuccessful
```

**使用`.get`**

```
[info] FirstTest:
[info] option
[info] - should value *** FAILED ***
[info]   java.util.NoSuchElementException: None.get
[info]   at scala.None$.get(Option.scala:349)
[info]   at scala.None$.get(Option.scala:347)
[info]   at first.FirstTest.$anonfun$new$6(FirstTest.scala:31)
[info]   at scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:12)
[info]   at org.scalatest.OutcomeOf.outcomeOf(OutcomeOf.scala:85)
[info]   at org.scalatest.OutcomeOf.outcomeOf$(OutcomeOf.scala:83)
[info]   at org.scalatest.OutcomeOf$.outcomeOf(OutcomeOf.scala:104)
[info]   at org.scalatest.Transformer.apply(Transformer.scala:22)
[info]   at org.scalatest.Transformer.apply(Transformer.scala:20)
[info]   at org.scalatest.WordSpecLike$$anon$1.apply(WordSpecLike.scala:1078)
[info]   ...
[info] Run completed in 211 milliseconds.
[info] Total number of tests run: 3
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 2, failed 1, canceled 0, ignored 0, pending 0
[info] *** 1 TEST FAILED ***
[error] Failed tests:
[error] 	first.FirstTest
[error] (Test / testOnly) sbt.TestsFailedException: Tests unsuccessful
```

## ScalaFutures

ScalaTest支持对异步代码进行阻塞测试。提供了隐式方法 `futureValue` 来从 `Future[T]` 中阻塞获取结果。

```
override implicit def patienceConfig = PatienceConfig(Span(60, Seconds), Span(50, Millis))

  "future" should {
    "await result === 3" in {
      import scala.concurrent.ExecutionContext.Implicits.global
      val f = Future{
        Thread.sleep(1000)
        3
      }
      val result = f.futureValue
      result shouldBe 3
    }
  }
```

上面代码的运行效果如下：

```
[info] FirstTest:
[info] future
[info] - should await result === 3
[info] Run completed in 1 second, 169 milliseconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
```

## Mock

ScalaTest为以下4种Mock提供了原生的支持：

1. [ScalaMock](http://www.scalamock.org/)
2. EasyMock
3. JMock
4. Mockito

这里先简单介绍下 **ScalaMock**。

ScalaMock是由 Paul Butcher 编写的一个原生的开源Scala Mocking框架，允许模拟对象和函数。ScalaMock支持3种不同的模拟风格：

- 函数模拟（Function mocks）
- 代理（动态）模拟（Proxy (dynamic) mocks）
- 生成类型安全模拟（Generated (type-safe) mocks）

**函数模拟**

```
  "scalamock" should {
    "function mock" in {
      val m = mockFunction[Int, String]
      m expects 42 returning "Forty two"
      m(42) shouldBe "Forty two"
    }
  }
```

这里我们模拟了一个函数 `m`，它接受一个Int参数并返回一个字符串值。这里看到，我们并没有实际定义这样一个函数，而是使用 `m expects 42 returning "Forty two"` 声明了这个模拟期待一个输入值：42，并返回结果字符串：`Forty row`。执行这个测试，运行效果如下：

```
[info] FirstTest:
[info] scalamock
[info] - should function mock
[info] Run completed in 211 milliseconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
```

