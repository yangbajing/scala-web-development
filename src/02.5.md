# class和object

## Class

Scala里也有`class`关键字，不过它定义类的方式与Java有些区别。Scala中，类默认是**public**的，且类属性和方法默认也是**public**的。Scala中，每个类都有一个**“主构造函数”**，主构造函数类似函数参数一样写在类名后的小括号中。因为Scala没有像Java那样的“构造函数”，所以属性变量都会在类被创建后初始化。所以当你需要在构造函数里初始化某些属性或资源时，写在类中的属性变量就相当于构造初始化了。

在Scala中定义类非常简单：

```scala
class Person(name: String, val age: Int) {
  override def toString(): String = s"姓名：$name, 年龄: $age"
}
```

默认，Scala主构造函数定义的属性是**private**的，可以显示指定：`val`或`var`来使其可见性为：**public**。

Scala中覆写一个方法必需添加：`override`关键字，这对于Java来说可以是一个修正。当标记了`override`关键字的方法在编译时，若编译器未能在父类中找到可覆写的方法时会报错。而在Java中，你只能通过`@Override`注解来实现类似功能，它的问题是它只是一个可选项，且编译器只提供警告。这样你还是很容易写出错误的“覆写”方法，你以后覆写了父类函数，但其实很有可能你是实现了一个新的方法，从而引入难以察觉的BUG。

实例化一个类的方式和Java一样，也是使用`new`关键字。

```scala
scala> val me = new Person("杨景", 30)
me: Person = 姓名：杨景, 年龄: 30

scala> println(me)
姓名：杨景, 年龄: 30

scala> me.name
<console>:20: error: value name is not a member of Person
       me.name
          ^

scala> me.age
res11: Int = 30
```

**case class（样本类）**

`case class`是Scala中学用的一个特性，像`Kotlin`这样的语言也学习并引入了类似特性（在`Kotlin`中叫做：`data class`）。`case class`具有如下特性：

1. 不需要使用`new`关键词创建，直接使用类名即可
2. 默认变量都是**public final**的，不可变的。当然也可以显示指定`var`、`private`等特性，但一般不推荐这样用
3. 自动实现了：`equals`、`hashcode`、`toString`等函数
4. 自动实现了：`Serializable`接口，默认是可序列化的
5. 可应用到**match case**（模式匹配）中
6. 自带一个`copy`方法，可以方便的根据某个`case class`实例来生成一个新的实例
7. ……

这里给出一个`case class`的使用样例：

```scala
scala> trait Person
defined trait Person

scala> case class Man(name: String, age: Int) extends Person
defined class Man

scala> case class Woman(name: String, age: Int) extends Person
defined class Woman

scala> val man = Man("杨景", 30)
man: Man = Man(杨景,30)

scala> val woman = Woman("女人", 23)
woman: Woman = Woman(女人,23)

scala> val manNextYear = man.copy(age = 31)
manNextYear: Man = Man(杨景,31)
```

## object

Scala有一种不同于Java的特殊类型，**Singleton Objects**。

```scala
object Blah {
  def sum(l: List[Int]): Int = l.sum
}
```

在Scala中，没有Java里的**static**静态变量和静态作用域的概念，取而代之的是：**object**。它除了可以实现Java里**static**的功能，它同时还是一个线程安全的单例类。

**伴身对象**

大多数的`object`都不是独立的，通常它都会与一个同名的`class`定义在一起。这样的`object`称为**伴身对象**。

```scala
class IntPair(val x: Int, val y: Int)

object IntPair {
  import math.Ordering
  implicit def ipord: Ordering[IntPair] =
    Ordering.by(ip => (ip.x, ip.y))
}
```

***注意***

**伴身对象**必需和它关联的类定义定义在同一个**.scala**文件。

伴身对象和它相关的类之间可以相互访问受保护的成员。在Java程序中，很多时候会把**static**成员设置成**private**的，在Scala中需要这样实现此特性：

```scala
class X {
  import X._
  def blah = foo
}
object X {
  private def foo = 42
}
```
