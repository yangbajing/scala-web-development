# 集合

在`java.util`包下有丰富的集合库。Scala除了可以使用Java定义的集合库外，它还自己定义了一套功能强大、特性丰富的`scala.collection`集合库API。

在Scala中，常用的集合类型有：`List`、`Set`、`Map`、`Tuple`、`Vector`等。

**List**

Scala中`List`是一个不可变列表集合，它很精妙的使用递归结构定义了一个列表集合。

```scala
scala> val list = List(1, 2, 3, 4, 5)
list: List[Int] = List(1, 2, 3, 4, 5)
```

除了之前使用`List`object来定义一个列表，还可以使用如下方式：

```scala
scala> val list = 1 :: 2 :: 3 :: 4 :: 5 :: Nil
list: List[Int] = List(1, 2, 3, 4, 5)
```

`List`采用前缀操作的方式（所有操作都在列表顶端（开头））进行，`::`操作符的作用是将一个元素和列表连接起来，并把元素放在列表的开头。这样`List`的操作就可以定义成一个递归操作。添加一个元素就是把元素加到列表的开头，List只需要更改下头指针，而删除一个元素就是把List的头指针指向列表中的第2个元素。这样，`List`的实现就非常的高效，它也不需要对内存做任何的转移操作。`List`有很多常用的方法：

```scala
scala> list.indexOf(3)
res6: Int = 2

scala> 0 :: list
res8: List[Int] = List(0, 1, 2, 3, 4, 5)

scala> list.reverse
res9: List[Int] = List(5, 4, 3, 2, 1)

scala> list.filter(item => item == 3)
res11: List[Int] = List(3)

scala> list
res12: List[Int] = List(1, 2, 3, 4, 5)

scala> val list2 = List(4, 5, 6, 7, 8, 9)
list2: List[Int] = List(4, 5, 6, 7, 8, 9)

scala> list.intersect(list2)
res13: List[Int] = List(4, 5)

scala> list.union(list2)
res14: List[Int] = List(1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9)

scala> list.diff(list2)
res15: List[Int] = List(1, 2, 3)
```

Scala中默认都是**Immutable collection**，在集合上定义的操作都不会更改集合本身，而是生成一个新的集合。这与Java集合是一个根本的区别，Java集合默认都是可变的。

**Tuple**

Scala中也支持**Tuple**（元组）这种集合，但最多只支持22个元素（事实上Scala中定义了`Tuple0`、`Tuple1`……`Tuple22`这样22个`TupleX`类，实现方式与`C++ Boost`库中的`Tuple`类似）。和大多数语言的Tuple类似（比如：Python），Scala也采用小括号来定义元组。

```scala
scala> val tuple3 = (1, 2, 3)
tuple1: (Int, Int, Int) = (1,2,3)

scala> tuple3._2
res17: Int = 2

scala> val tuple2 = Tuple2("杨", "景")
tuple2: (String, String) = (杨,景)
```

可以使用`xxx._[X]`的形式来引用`Tuple`中某一个具体元素，其`_[X]`下标是从1开始的，一直到22（若有定义这么多）。

**Set**

`Set`是一个不重复且无序的集合，初始化一个`Set`需要使用`Set`对象：

```scala
scala> val set = Set("Scala", "Java", "C++", "Javascript", "C#", "Python", "PHP") 
set: scala.collection.immutable.Set[String] = Set(Scala, C#, Python, Javascript, PHP, C++, Java)

scala> set + "Go"
res21: scala.collection.immutable.Set[String] = Set(Scala, C#, Go, Python, Javascript, PHP, C++, Java)

scala> set filterNot (item => item == "PHP")
res22: scala.collection.immutable.Set[String] = Set(Scala, C#, Python, Javascript, C++, Java)
```

**Map**

Scala中的`Map`默认是一个**HashMap**，其特性与Java版的`HashMap`基本一至，除了它是`Immutable`的：

```scala
scala> val map = Map("a" -> "A", "b" -> "B")
map: scala.collection.immutable.Map[String,String] = Map(a -> A, b -> B)

scala> val map2 = Map(("b", "B"), ("c", "C"))
map2: scala.collection.immutable.Map[String,String] = Map(b -> B, c -> C)
```

Scala中定义`Map`时，传入的每个`Entry`（**K**、**V**对）其实就是一个`Tuple2`（有两个元素的元组），而`->`是定义`Tuple2`的一种便捷方式。

```scala
scala> map + ("z" -> "Z")
res23: scala.collection.immutable.Map[String,String] = Map(a -> A, b -> B, z -> Z)

scala> map.filterNot(entry => entry._1 == "a")
res24: scala.collection.immutable.Map[String,String] = Map(b -> B)

scala> val map3 = map - "a"
map3: scala.collection.immutable.Map[String,String] = Map(b -> B)

scala> map
res25: scala.collection.immutable.Map[String,String] = Map(a -> A, b -> B)
```

Scala的immutable collection并没有添加和删除元素的操作，其定义`+`（`List`使用`::`在头部添加）操作都是生成一个新的集合，而要删除一个元素一般使用 `-` 操作直接将**Key**从`map`中减掉即可。

（注：Scala中也`scala.collection.mutable._`集合，它定义了不可变集合的相应可变集合版本。一般情况下，除非一此性能优先的操作（其实Scala集合采用了共享存储的优化，生成一个新集合并不会生成所有元素的复本，它将会和老的集合共享大元素。因为Scala中变量默认都是不可变的），推荐还是采用不可变集合。因为它更直观、线程安全，你可以确定你的变量不会在其它地方被不小心的更改。）
