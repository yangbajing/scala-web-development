# 使用Guice管理类依赖

当项目规模大了以后，依赖管理是一个很头疼的问题。手工管理众多类的实例化和构造顺序是一个枯燥且容易出错的事情。在Java世界，JSR-330规范（Java依赖注入规范）定义了一套标准的接口来实现此功能。
该规范主要是面向依赖注入使用者，而对注入器实现、配置并未作详细要求。目前 Spring、Guice实现了该规范，JSR-299（Contexts and Dependency Injection for Java EE platform，参考实现 Weld）在依赖注入上也使用该规范。

## JSR-330

### javax.inject

`javax.inject`包指定了获取、实例化对象的方法，该方法与构造器、工厂以及命名服务查找（例如JNDI）这些传统方法相比可以获得更好的可重用性、可测试性以及可维护性。

通常在程序中，很多类型需要依赖于其它类型。例如，一个`Stopwatch`类可能依赖地一个`TimeSource`。一些类型补另一个类型依赖，我们就把这些类型叫做这个类型的 **依赖（物）**。在运行时查找一个依赖实例的过程叫做 **依赖解析**。如何找不到依赖的实例，那我们称该依赖为 **不能满足**，并导致应用运行失败。

在不使用依赖注入时，对象的依赖解析有多种方式。最常见的就是通过在构造器内直接硬编码实现：
```java
class Stopwatch {
    final TimeSource timeSource;

    Stopwatch() {
        timeSource = new AtomicClocl();
    }

    void start() { .... }


    long stop() { .... }
}
```

若需要更有弹性一点，那么可能通过工厂或服务查询的方式实现。
```java
class Stopwatch {
    final TimeSource timeSource;

    Stopwatch() {
        timeSource = TimeSourceFactory.getInstance();
    }

    // ....
}
```

这种方式是不可重用的，且可测试性非常低，也不利于代码的维护。而 **依赖注入** 就是为了解决这类问题。代替程序员调用构造器或工厂，一个称作 **依赖注入器** 的工具将把依赖传递给对象：
```scala
class Stopwatch @Inject()(timeSource: TimeSource) {
  def start(): Unit = { .... }

  def stop(): Long = { .... }
}
```

注入器将更进一步地传递依赖给其他的依赖，直到它构造出整个对象图。如：
```scala
class StopwatchWidget @Inject()(sw: Stopwatch) {
  // ....
}
```

注入器可能的操作为：

1. 查找一个`TimeSource`实例
2. 使用找到的`TimeSource`实例构造一个`Stopwatch`
3. 使用构造的`Stopwatch`实例构造一个`StopwatchWidget`

这可以使用我们的代码保持干净。同时，在单元测试时，可以直接将模拟对象传入构造器（不使用依赖注入框架）。
```scala
def testStopwatch() {
  val sw = new Stopwatch(new MockTimeSource())
}
```

在`javax.inject`中提供了一些注解来简化我们使用依赖注入。

### @Inject

注解`@Inject`标识了可注入的构造器、方法或属性字段。可以用于静态或实例成员，一个可注入的成员可以被任何访问修饰符进行修饰（private、package-private、protected、public）。注入的顺序为构造器、属性字段、方法。超类的属性、方法将优先于子类的属性、方法被注入。对于同一个类的属性是不区分注入顺序的，同一个类的方法亦一样。

对于每一个类，`@Inject`最多只允许对一个构造器进行标注（Scala一般是在主构造器上标注）。

当一个方法标注了`@Inject`并覆写了基类标注了`@Inject`的方法时，对于每一个实例可注入一次。当一个方法没有标注`@Inject`并覆写了基类标注了`@Inject`的方法时，依赖注入框架不会对此方法进行注入。

@@@note { title=注意 }
1. 在Scala中，若使用构造器注入。需要使用`@Inject()`，它和Java里面直接写`@Inject`不一样。
2. 当使用属性字段注入时，属性通常使用`var`定义。
@@@