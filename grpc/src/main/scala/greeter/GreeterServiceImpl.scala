/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package greeter

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{ Sink, Source }

import scala.concurrent.Future

// #GreeterServiceImpl
class GreeterServiceImpl()(implicit system: ActorSystem[_]) extends GreeterService {
  import system.executionContext

  override def sayHello(in: HelloRequest): Future[HelloReply] = {
    Future.successful(HelloReply(s"Hello, ${in.name}."))
  }

  override def itKeepsTalking(
      in: Source[HelloRequest, NotUsed]): Future[HelloReply] = {
    in.runWith(Sink.seq)
      .map(ins => HelloReply("Hello, " + ins.map(_.name).mkString("", ", ", ".")))
  }

  override def itKeepsReplying(in: HelloRequest): Source[HelloReply, NotUsed] = {
    Source
      .fromIterator(() => Iterator.from(1))
      .map(n => HelloReply(s"Hello, ${in.name}; this is $n times."))
  }

  override def streamHellos(
      ins: Source[HelloRequest, NotUsed]): Source[HelloReply, NotUsed] = {
    ins.map(in => HelloReply(s"Hello, ${in.name}."))
  }
}
// #GreeterServiceImpl
