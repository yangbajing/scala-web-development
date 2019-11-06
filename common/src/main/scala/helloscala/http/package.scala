package helloscala

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.SourceQueueWithComplete

import scala.concurrent.Promise

package object http {
  type HttpSourceQueue = SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])]

  class AkkaHttpSourceQueue(val httpSourceQueue: HttpSourceQueue)
}
