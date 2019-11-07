# Akka Typed Actor

## Hello Scala!

Akka Typed Actor从2.4开始直到2.5可以商用，进而Akka 2.6已经把Akka Typed Actor做为推荐的Actor使用模式。Typed Actor与原先的Untyped Actor最大的区别Actor有类型了，其签名也改成了`akka.actor.typed.ActorRef[T]`。通过一个简单的示例来看看在Akka Typed环境下怎样使用Actor。

@@snip [HelloScala.scala](../../../test/scala/book/typed/HelloScala.scala) { #HelloScala }

Akka Typed不再需要通过类的形式来实现`Actor`接口定义，而是函数的形式来定义actor。可以看到，定义的actor类型为`Behavior[T]`（**形为**），通过`Behaviors.receiveMessage[T](T => Behavior[T]): Receive[T]`函数来处理接收到的消息，而`Receive`继承了`Behavior` trait。通过函数签名可以看到，每次接收到消息并对其处理完成后，都必需要返回一个新的形为。

`apply(): Behavior[Command]`函数签名里的范性参数类型`Command`限制了这个actor将只接收`Command`或`Command`子类型的消息，编译器将在编译期对传给actor的消息做类型检查，相对于从前的untyped actor可以向actor传入任何类型的消息，这可以限制的减少程序中的bug。特别是在程序规模很大，当你定义了成百上千个消息时。

也因为有类型的actor，在Akka Typed中没有了隐式发送的`sender: ActorRef`，必需在发送的消息里面包含回复字段，就如`Hello`消息定义里的`replyTo: ActorRef[Reply]`字段一样。actor在处理完`Hello`消息后可以通过它向发送者回复处理结果。

@@snip [HelloScala.scala](../../../test/scala/book/typed/HelloScala.scala) { #HelloScalaSpec }

## 更复杂的一个示例

上一个示例简单的演示了Akka Typed Actor的功能和基本使用方式，接下来看一个更复杂的示例，将展示Akka Typed更多的特性及功能。

首先是消息定义：

```scala
  sealed trait Command
  trait ControlCommand extends Command { val clientId: String }
  trait ReplyCommand extends Command { val replyTo: ActorRef[Reply] }

  final case class Connect(clientId: String, replyTo: ActorRef[Reply]) extends ControlCommand with ReplyCommand
  final case class Disconnect(clientId: String, replyTo: ActorRef[Reply]) extends ControlCommand with ReplyCommand
  final case class QueryResource(clientId: String, replyTo: ActorRef[Reply]) extends ReplyCommand
  final private[typed] case object SessionTimeout extends Command
  final private case class ServiceKeyRegistered(registered: Receptionist.Registered) extends Command

  sealed trait Reply
  final case class Connected(status: Int, clientId: String) extends Reply
  final case class Disconnected(status: Int, clientId: String) extends Reply
  final case class ResourceQueried(status: Int, clientId: String, resources: Seq[String]) extends Reply
  final case class ReplyError(status: Int) extends Reply
```

上面分别定义了actor可接收的请求消息：`Command`和返回结果消息：`Reply`。建议对于需要返回值的消息使用：`replyTo`来命名收受返回值的actor字段，这里也可以不定义`Reply` trait来做为统一的返回值类型，可以直接返回结果类型，如：`ActorRef[String`。

这里将定义两个actor，一个做为父actor，一个做为子actor。父actor为：`ComplexActor`，管理连接客户端和转发消息到子actor，每次有新的客户端连接上来时做以客户端`clientId`做为名字创建一个子actor；子actor：`ComplexClient`，保持客户端连接会话，处理消息……

**ComplexActor**
```scala
final class ComplexActor private(context: ActorContext[ComplexActor.Command]) {
  import ComplexActor._
  private var connects = Map.empty[String, ActorRef[Command]]

  def init(): Behavior[Command] = Behaviors.receiveMessage {
    case ServiceKeyRegistered(registered) if registered.isForKey(serviceKey) =>
      context.log.info("Actor be registered, serviceKey: {}", serviceKey)
      receive()
    ....
  }

  def receive(): Behavior[Command] =
    Behaviors
      .receiveMessage[Command] {
        case cmd @ Connect(clientId, replyTo) =>
          if (connects.contains(clientId)) {
            replyTo ! Connected(IntStatus.CONFLICT, clientId)
          } else {
            val child = context.spawn(
              Behaviors
                .supervise(ComplexClient(clientId))
                .onFailure(SupervisorStrategy.restart),
              clientId)
            context.watch(child)
            connects = connects.updated(clientId, child)
            child ! cmd
          }
          Behaviors.same
        ....
      }
      .receiveSignal {
        case (_, Terminated(child)) =>
          val clientId = child.path.name
          connects -= clientId
          context.unwatch(child)
          Behaviors.same
      }
}
```

`ComplexActor`在收到`Connect`消息后将首先判断请求客户端ID（`clientId`）是否已经连接，若重复连接将直接返回409错误（`Connected(IntStatus.CONFLICT, _)`）。若是一个新连接将调用`context.spawn`函数在创建一个字actor：`ComplexClient`。`spawn`函数签名如下：

```scala
def spawn[U](behavior: Behavior[U], name: String, props: Props = Props.empty): ActorRef[U]
```

`behavior`是要创建的actor，`name`为子actor的名字，需要保证在同一级内唯一（兄弟之间），`props`可对actor作一些自定义，如：线程执行器（`Dispatcher`）、邮箱等。

`receiveSignal`用于接收系统控制信号消息，经典actor的`preRestart`和`postStop`回调函数（将分别做为`PreRestart`和`PostStop`信号），以及`Terminated`消息都将做为信号发送到这里。

**ComplexClient**
```scala
final class ComplexClient private (
    clientId: String,
    context: ActorContext[ComplexActor.Command]) {
  import ComplexActor._

  def active(): Behavior[Command] = Behaviors.receiveMessagePartial {
    ....
    case SessionTimeout =>
      context.log.warn("Inactive timeout, stop!")
      Behaviors.stopped
  }

  def init(): Behavior[Command] = Behaviors.receiveMessage {
    case Connect(`clientId`, replyTo) =>
      replyTo ! Connected(IntStatus.OK, clientId)
      context.setReceiveTimeout(120.seconds, SessionTimeout)
      active()
    case other =>
      context.log.warn("Receive invalid command: {}", other)
      Behaviors.same
  }
```

`ComplexClient`定义了两个形为函数，`init()`和`active`。当客户端连接成功以后会返回`active()`函数作为actor新的形为来接收之后的消息。这种返回一个新的`Behavior`函数的形式替代了经典actor里的`become`、`unbecome`函数，它更直观，甚至还可以使用这种方式来实现**状态机**。

`context.setReceiveTimeout(120.seconds, SessionTimeout)`用来设置两次消息接收之间的超时时间，这里设备为120秒。可以通过方式来实现服务端会话（session）超时判断，当session超时时返回`Behaviors.stopped`消息来停止actor（自己）。这里需要注意的是`context.stop`只能用来停止直接子actor，停止actor自身返回`stopped`形为即可，这与经典actor有着明显的区别。

### 发现actor

Akka Typed取消了`actorSelection`函数，不再允许通过actor path路径来查找ActorRef。取而代之的是使用`Receptionist`机制来注册服务（actor实例）。也就是说，在Akka Typed中，actor默认情况下是不能查找的，只能通过引用（`ActorRef[T]`）来使用，要么actor之间具有父子关系，要么通过消息传递`ActorRef[T]`……

```scala
object ComplexActor {
  val serviceKey = ServiceKey[Command]("complex")

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    val registerAdapter = context.messageAdapter[Receptionist.Registered](value => ServiceKeyRegistered(value))
    context.system.receptionist ! Receptionist.Register(serviceKey, context.self, registerAdapter)
    new ComplexActor(context).init()
  }
}
```

上面代码通过`Receptionist.Register`将actor（`context.self`引用）以`serviceKey`注册到Actor系统的**receptionist**表，之后就可以通过`serviceKey`来发现并获取此actor的引用。

```scala
  val actorRef: ActorRef[ComplexActor.Command] = system.receptionist
    .ask[Receptionist.Listing](Receptionist.Find(ComplexActor.serviceKey))
    .map { listing =>
      if (listing.isForKey(serviceKey))
        listing.serviceInstances(serviceKey).head
      else 
        throw new IllegalAccessException(s"Actor reference not found: $serviceKey")
    }
```

### 消息适配器

有时候，需要将不匹配的消息发送给actor，比如：把receptionist服务注册结果 `Receptionist.Registered`发送给一个actor，我们可以通过将消息包装到一个实现了`Command` trait的case class来实现。如下面的代码示例：

```scala
val registerAdapter: ActorRef[Receptionist.Registered] =
  context.messageAdapter[Receptionist.Registered](value => ServiceKeyRegistered(value))
```

在使用`Receptionist.Register`时将`registerAdapter`作为第3个参数传入，这样服务注册结果就将被包装成`ServiceKeyRegistered`消息传给actor。

### 在actor内部处理异步任务

actor内部消息都是串行执行的，在actor内执行异步操作时需要小心。不能在`Future`的回调函数里直接操作actor内部变量，因为它们很可能在两个不同的线程中。

可以通过`context.pipeToSelf`将异步结果转换成一个消息传递给actor，这样异步结果将进入actor的邮箱列队，通过正确的消息处理机制来处理。

```scala
  case QueryResource(_, replyTo) =>
    context.pipeToSelf(findExternalResource())(value => InternalQueryResource(value, replyTo))
    Behaviors.same

  case InternalQueryResource(tryValue, replyTo) =>
    replyTo ! tryValue
      .map(ResourceQueried(IntStatus.OK, clientId, _))
      .getOrElse(ResourceQueried(IntStatus.INTERNAL_ERROR, clientId, Nil))
    Behaviors.same
```

## 在ActorSystem[_]外部创建actor

Akka Typed开始，`ActorSystem[T]`也拥有一个泛型参数，在构造ActorSystem时需要传入一个默认`Behavior[T]`，并将其作为经典actor下的user守卫（也就类似拥有`akka://system-name/user`这个路径的actor），同时`ActorSystem[T]`的`actorOf`函数也被取消。Akka Typed推荐应用都从传给ActorSystem的默认`Behavior[T]`开始构建actor树。但有时，也许通过`ActorSystem[T]`的实例来创建actor是有意义的，可以通过将typed的`ActorSystem[T]`转换成经典的untyped `ActorSystem`来实现。代码如下：

```scala
implicit val timeout = Timeout(2.seconds)
implicit val system: ActorSystem[_] = _ // ....

val spawnActor: ActorRef[SpawnProtocol.Command] = system.toClassic
  .actorOf(
    PropsAdapter(Behaviors.supervise(SpawnProtocol())
      .onFailure(SupervisorStrategy.resume)), "spawn")
  .toTyped[SpawnProtocol.Command]

val helloScalaF: Future[ActorRef[HelloScala.Command]] = 
  spawnActor.ask[ActorRef[HelloScala.Command]](replyTo =>
    SpawnProtocol.Spawn(HelloScala(), "sample", Props.empty, replyTo))

val helloScala: ActorRef[HelloScala.Command] = Await.result(helloScalaF, 2.seconds)
```

也可以将`SpawnProtocol()`作为`ActorSystem[_]`的初始`Behavior[T]`来构造ActorSystem，这样就可以通过`system.ask[ActorRef[T]](SpawnProtocol.Spawn(....))`来创建在user守卫下的actor了。

## 小结

本文通过两个例子展示了Akka Typed的特性，它与经典actor的区别还是挺大的。从untyped和typed，actor拥有了类型，这对于大规模actor系统开发可以在编译期发现很多重复，它将强制你在设计actor时首先考虑消息的定义。定义的消息即是actor之间的数据交互协议，消息定义的过程也是业务模式和模块划分的过程。

**完整示例代码**

**HelloScala.scala**

@@snip [HelloScala.scala](../../../test/scala/book/typed/HelloScala.scala) { #HelloScala-scala }

**ComplexActor.scala**

@@snip [ComplexActor.scala](../../../test/scala/book/typed/ComplexActor.scala) { #ComplexActor-scala }
