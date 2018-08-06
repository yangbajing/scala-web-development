# 函数式

## 函数

在Scala中，函数是一等公民。函数可以像类型一样被赋值给一个变量，也可以做为一个函数的参数被传入，甚至还可以做为函数的返回值返回。

从Java 8开始，Java也具备了部分函数式编程特性。其Lamdba函数允许将一个函数做值赋给变量、做为方法参数、做为函数返回值。

在Scala中，使用`def`关键ygnk来定义一个函数方法：

```scala
scala> def calc(n1: Int, n2: Int): (Int, Int) = {
     |   (n1 + n2, n1 * n2)
     | }
calc: (n1: Int, n2: Int)(Int, Int)

scala> val (add, sub) = calc(5, 1)
add: Int = 6
sub: Int = 5
```

这里定义了一个函数：`calc`，它有两个参数：`n1`和`n2`，其类型为：`Int`。`cala`函数的返回值类型是一个有两个元素的元组，在Scala中可以简写为：`(Int, Int)`。在Scala中，代码段的最后一句将做为函数返回值，所以这里不需要显示的写`return`关键字。

而`val (add, sub) = calc(5, 1)`一句，是Scala中的抽取功能。它直接把`calc`函数返回的一个`Tuple2`值赋给了`add`他`sub`两个变量。

函数可以赋给变量：

```scala
scala> val calcVar = calc _
calcVar: (Int, Int) => (Int, Int) = <function2>

scala> calcVar(2, 3)
res4: (Int, Int) = (5,6)

scala> val sum: (Int, Int) => Int = (x, y) => x + y
sum: (Int, Int) => Int = <function2>

scala> sum(5, 7)
res5: Int = 12
```

在Scala中，有两种定义函数的方式：

1. 将一个现成的函数/方法赋值给一个变量，如：`val calcVar = calc _`。下划线在此处的含意是将函数赋给了变量，函数本身的参数将在变量被调用时再传入。
2. 直接定义函数并同时赋给变量，如：`val sum: (Int, Int) => Int = (x, y) => x + y`，在冒号之后，等号之前部分：`(Int, Int) => Int`是函数签名，代表`sum`这个函数值接收两个Int类型参数并返回一个Int类型参数。等号之后部分是函数体，在函数函数时，`x`、`y`参数类型及返回值类型在此可以省略。

**一个函数示例：自动资源管理**

在我们的日常代码中，资源回收是一个很常见的操作。在Java 7之前，我们必需写很多的`try { ... } finally { xxx.close() }`这样的样版代码来手动回收资源。Java 7开始，提供了**try with close**这样的自动资源回收功能。Scala并不能使用Java 7新加的**try with close**资源自动回收功能，但Scala中有很方便的方式实现类似功能：

```scala
def using[T <: AutoCloseable, R](res: T)(func: T => R): R = {
  try {
    func(res)
  } finally {
    if (res != null)
      res.close()
  }
}

val allLine = using(Files.newBufferedReader(Paths.get("/etc/hosts"))) { reader =>
  @tailrec
  def readAll(buffer: StringBuilder, line: String): String = {
    if (line == null) buffer.toString
    else {
      buffer.append(line).append('\n')
      readAll(buffer, reader.readLine())
    }
  }
  
  readAll(new StringBuilder(), reader.readLine())
}

println(allLine)
```

`using`是我们定义的一个自动化资源管帮助函数，它接爱两个参数化类型参数，一个是实现了`AutoCloseable`接口的资源类，一个是形如：`T => R`的函数值。`func`是由用户定义的对`res`进行操作的函数代码体，它将被传给`using`函数并由`using`代执行。而`res`这个资源将在`using`执行完成返回前调用`finally`代码块执行`.close`方法来清理打开的资源。

这个：`T <: AutoCloseable`范型参数限制了**T**类型必需为`AutoCloseable`类型或其子类。`R`范型指定`using`函数的返回值类型将在实际调用时被自动参数化推导出来。我们在**Scala Console**中参看`allLine`变量的类型可以看到 `allLine`将被正确的赋予**String**类型，因为我们传给`using`函数参数`func`的函数值返回类型就为**String**：

```scala
scala> :type allLine
String
```

在`readAll`函数的定义处，有两个特别的地方：

1. 这个函数定义在了其它函数代码体内部
2. 它有一个`@tailrec`注解

在Scala中，因为函数是第一类的，它可以被赋值给一个变量。所以Scala中的`def`定义函数可以等价`val func = (x: Int, y: Int) => x + y`这个的函数字面量定义函数形式。所以，既然通过变量定义的函数可以放在其它函数代码体内，通过`def`定义的函数也一样可以放在其它代码体内，这和**Javascript**很像。

`@tailrec`注解的含义是这个函数是尾递归函数，编译器在编译时将对其优化成相应的**while**循环。若一个函数不是尾递归的，加上此注解在编译时将报错。

## 模式匹配（match case）

模式匹配是函数式编程里面很强大的一个特性。

之前已经见识过了模式匹配的简单使用方式，可以用它替代：**if else**、**switch**这样的分支判断。除了这些简单的功能，模式匹配还有一系列强大、易用的特性。

**match 中的值、变量和类型**

```scala
scala> for {
     |   x <- Seq(1, false, 2.7, "one", 'four, new java.util.Date(), new RuntimeException("运行时异常"))
     | } {
     |   val str = x match {
     |     case d: Double => s"double: $d"
     |     case false => "boolean false"
     |     case d: java.util.Date => s"java.util.Date: $d"
     |     case 1 => "int 1"
     |     case s: String => s"string: $s"
     |     case symbol: Symbol => s"symbol: $symbol"
     |     case unexpected => s"unexpected value: $unexpected"
     |   }
     |   println(str)
     | }
int 1
boolean false
double: 2.7
string: one
symbol: 'four
java.util.Date: Sun Jul 24 16:51:20 CST 2016
unexpected value: java.lang.RuntimeException: 运行时异常
```

上面小试牛刀校验变量类型的同时完成类型转换功能。在Java中，你肯定写过或见过如下的代码：

```java
public void receive(message: Object) {
    if (message isInstanceOf String) {
        String strMsg = (String) message;
        ....
    } else if (message isInstanceOf java.util.Date) {
        java.util.Date dateMsg = (java.util.Date) message;
        ....
    } ....
}
```

对于这样的代码，真是辣眼睛啊~~~。

**序列的匹配**

```scala
scala> val nonEmptySeq = Seq(1, 2, 3, 4, 5)

scala> val emptySeq = Seq.empty[Int]

scala> val emptyList = Nil

scala> val nonEmptyList = List(1, 2, 3, 4, 5)

scala> val nonEmptyVector = Vector(1, 2, 3, 4, 5)

scala> val emptyVector = Vector.empty[Int]

scala> val nonEmptyMap = Map("one" -> 1, "two" -> 2, "three" -> 3)

scala> val emptyMap = Map.empty[String, Int]

scala> def seqToString[T](seq: Seq[T]): String = seq match {
     |   case head +: tail => s"$head +: " + seqToString(tail)
     |   case Nil => "Nil"
     | }

scala> for (seq <- Seq(
     |   nonEmptySeq, emptySeq, nonEmptyList, emptyList,
     |   nonEmptyVector, emptyVector, nonEmptyMap.toSeq, emptyMap.toSeq)) {
     |   println(seqToString(seq))
     | }
1 +: 2 +: 3 +: 4 +: 5 +: Nil
Nil
1 +: 2 +: 3 +: 4 +: 5 +: Nil
Nil
1 +: 2 +: 3 +: 4 +: 5 +: Nil
Nil
(one,1) +: (two,2) +: (three,3) +: Nil
Nil
```

模式匹配能很方便的抽取序列的元素，`seqToString`使用了模式匹配以递归的方式来将序列转换成字符串。`case head +: tail`将序列抽取成“头部”和“非头部剩下”两部分，`head`将保存序列第一个元素，`tail`保存序列剩下部分。而`case Nil`将匹配一个空序列。

**case class的匹配**

```scala
scala> trait Person

scala> case class Man(name: String, age: Int) extends Person

scala> case class Woman(name: String, age: Int) extends Person

scala> case class Boy(name: String, age: Int) extends Person

scala> val father = Man("父亲", 33)

scala> val mather = Woman("母亲", 30)

scala> val son = Man("儿子", 7)

scala> val daughter = Woman("女儿", 3)

scala> for (person <- Seq[Person](father, mather, son, daughter)) {
     |   person match {
     |     case Man("父亲", age) => println(s"父亲今年${age}岁")
     |     case man: Man if man.age < 10 => println(s"man is $man")
     |     case Woman(name, 30) => println(s"${name}今年有30岁")
     |     case Woman(name, age) => println(s"${name}今年有${age}岁")
     |   }
     | }
父亲今年33岁
母亲今年有30岁
man is Man(儿子,7)
女儿今年有3岁
```

在模式匹配中对`case class`进行**解构**操作，可以直接提取出感兴趣的字段并赋给变量。同时，模式匹配中还可以使用**guard**语句，给匹配判断添加一个`if`表达式做条件判断。
