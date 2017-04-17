# 流程和函数

## 控制语句（表达式）

Scala中支持`if`、`while`、`for comprehension`（for表达式)、`match case`（模式匹配）四大主要控制语句。Scala不支持`switch`和`? :`两种控制语句，但它的`if`和`match case`会有更好的实现。

**`if`**

Scala支持`if`语句，其基本使用和`Java`、`Python`中的一样。但不同的时，它是有返回值的。

（注：Scala是函数式语言，函数式语言还有一大特性就是：表达式。函数式语言中所有语句都是基于“表达式”的，而“表达式”的一个特性就是它会有一个值。所有像`Java`中的`? :`3目运算符可以使用`if`语句来代替）。

```scala
scala> if (true) "真" else "假"
res0: String = 真

scala> val f = if (false) "真" else "假"
f: String = 假

scala> val unit = if (false) "真"
unit: Any = ()

scala> val unit2 = if (true) "真" 
unit2: Any = 真
```

***可以看到，`if`语句也是有返回值的，将表达式的结果赋给变量，编译器也能正常推导出变量的类型。***`unit`和`unit2`变量的类型是`Any`，这是因为`else`语句的缺失，Scala编译器就按最大化类型来推导，而`Any`类型是Scala中的根类型。`()`在Scala中是`Unit`类型的实例，可以看做是`Java`中的`Void`。

**`while`**

Scala中的`while`循环语句：

```scala
while (条件) {
  语句块
}
```

**`for comprehension`**

Scala中也有`for`表达式，但它和`Java`中的`for`不太一样，它具有更强大的特性。通常的`for`语句如下：

```scala
for (变量 <- 集合) {
  语句块
}
```

Scala中`for`表达式除了上面那样的常规用法，它还可以使用`yield`关键字将集合映射为另一个集合：

```scala
scala> val list = List(1, 2, 3, 4, 5)
list: List[Int] = List(1, 2, 3, 4, 5)

scala> val list2 = for (item <- list) yield item + 1
list2: List[Int] = List(2, 3, 4, 5, 6)
```

还可以在表达式中使用`if`判断：

```scala
scala> val list3 = for (item <- list if item % 2 == 0) yield item
list3: List[Int] = List(2, 4)
```

还可以做`flatMap`操作，解析2维列表并将结果摊平（将2维列表拉平为一维列表）：

```scala
scala> val llist = List(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9))
llist: List[List[Int]] = List(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9))

scala> for {
     |   l <- llist
     |   item <- l if item % 2 == 0
     | } yield item
res3: List[Int] = List(2, 4, 6, 8)
```

看到了，Scala中`for comprehension`的特性是很强大的。Scala的整个集合库都支持这一特性，包括：`Seq`、`Map`、`Set`、`Array`……

Scala没有C-Like语言里的`for (int i = 0; i < 10; i++)`语法，但`Range`（范围这个概念），可以基于它来实现循环迭代功能。在Scala中的使用方式如下：

```scala
scala> for (i <- (0 until 10)) {
     |   println(i)
     | }
0
1
2
3
4
5
6
7
8
9
```

Scala中还有一个`to`方法：

```scala
scala> for (i <- (0 to 10)) print(" " + i)
 0 1 2 3 4 5 6 7 8 9 10
```

你还可以控制每次步进的步长，只需要简单的使用`by`方法即可：

```scala
scala> for (i <- 0 to 10 by 2) print(" " + i)
 0 2 4 6 8 10
```

**match case**

模式匹配，是函数式语言很强大的一个特性。它比命令式语言里的`switch`更好用，表达性更强。

```scala
scala> def level(s: Int) = s match {
     |   case n if n >= 90 => "优秀"
     |   case n if n >= 80 => "良好"
     |   case n if n >= 70 => "良"
     |   case n if n >= 60 => "及格"
     |   case _ => "差"
     | }
level: (s: Int)String

scala> level(51)
res28: String = 差

scala> level(93)
res29: String = 优秀

scala> level(80)
res30: String = 良好
```

可以看到，模式匹配可以实现`switch`相似的功能。但与`switch`需要使用`break`明确告知终止之后的判断不同，Scala中的`match case`是默认**break**的。只要其中一个`case`语句匹配，就终止之后的所以比较。且对应`case`语句的表达式值将作为整个`match case`表达式的值返回。

Scala中的模式匹配还有类型匹配、数据抽取、谓词判断等其它有用的功能。这里只做简单介绍，之后会单独一个章节来做较详细的解读。
