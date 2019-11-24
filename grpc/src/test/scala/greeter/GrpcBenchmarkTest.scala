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

import java.util.concurrent.TimeUnit

import akka.actor.typed.ActorSystem
import akka.actor.typed.SpawnProtocol
import akka.grpc.GrpcClientSettings
import akka.actor.typed.scaladsl.adapter._
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object GrpcBenchmarkTest {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[SpawnProtocol.Command] =
      ActorSystem(SpawnProtocol(), "grpc-test")
    import system.executionContext
    val greeterServiceClient = GreeterServiceClient(
      GrpcClientSettings.fromConfig(GreeterService.name)(system.toClassic))
    val greeterServiceClients =
      Vector.fill(4)(
        GreeterServiceClient(
          GrpcClientSettings.fromConfig(GreeterService.name)(system.toClassic)))

    def bench(n: Int): Unit = {
      val begin = System.nanoTime()
      val f = Source
        .fromIterator(() => Iterator.from(0))
        .take(n)
        .mapAsync(240) { n =>
          val i = n % greeterServiceClients.size
          greeterServiceClients(i).sayHello(HelloRequest(s"this is $n"))
//          greeterServiceClient.sayHello(HelloRequest(s"this is $n"))
        }
        .runWith(Sink.ignore)
      f.onComplete { tryValue =>
        val millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin)
        val sec = n * 1000 / millis
        println(
          s"Sending $n messages takes ${millis}ms, TPS: $sec/s. return: $tryValue")
      }
      Await.ready(f, Duration.Inf)
    }

    def benchStreaming(n: Int): Unit = {
      val begin = System.nanoTime()
      val f = greeterServiceClient.itKeepsTalking(
        Source.repeat(0).map(n => HelloRequest(s"this is $n")).take(n))
      f.onComplete { tryValue =>
        val millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin)
        val sec = n * 1000 / millis
        println(
          s"Streaming sending $n messages takes ${millis}ms, TPS: $sec/s. return: $tryValue")
      }
      Await.ready(f, Duration.Inf)
    }
//    benchStreaming(5_0000)
//    benchStreaming(20_0000)
//    benchStreaming(20_0000)

    bench(5_0000)
    bench(20_0000)
    bench(20_0000)

    system.terminate()
    Await.ready(system.whenTerminated, Duration.Inf)
  }
}
