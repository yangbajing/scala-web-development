package helloscala.http.route

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.CacheDirectives.`no-cache`
import akka.http.scaladsl.model.headers.CacheDirectives.`no-store`
import akka.http.scaladsl.server.PathMatcher.Matched
import akka.http.scaladsl.server.PathMatcher.Unmatched
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.FileInfo
import akka.http.scaladsl.server.util.Tuple
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import helloscala.common.data.ApiResult
import helloscala.common.exception.HSBadRequestException
import helloscala.common.exception.HSException
import helloscala.common.exception.HSNotFoundException
import helloscala.common.types.ObjectId
import helloscala.common.util.TimeUtils
import helloscala.http.AkkaHttpSourceQueue
import helloscala.http.HttpUtils
import message.oauth.GrantType
import message.oauth.ResponseType

import scala.annotation.tailrec
import scala.collection.immutable
import scala.concurrent.Future

trait AbstractRoute extends Directives {
  def route: Route

  implicit def grantTypeFromStringUnmarshaller: FromStringUnmarshaller[GrantType] =
    Unmarshaller.strict[String, GrantType](str =>
      GrantType.fromName(str).getOrElse(throw HSBadRequestException("$str 不是有效的GrantType类型")))

  implicit def responseTypeFromStringUnmarshaller: FromStringUnmarshaller[ResponseType] =
    Unmarshaller.strict[String, ResponseType](str =>
      ResponseType.fromName(str).getOrElse(throw HSBadRequestException("$str 不是有效的GrantType类型")))

  implicit def objectIdFromStringUnmarshaller: FromStringUnmarshaller[ObjectId] =
    Unmarshaller.strict[String, ObjectId] {
      case str if ObjectId.isValid(str) => ObjectId.apply(str)
      case str                          => throw HSBadRequestException(s"$str 不是有效的ObjectId字符串")
    }

  implicit def localDateFromStringUnmarshaller: FromStringUnmarshaller[LocalDate] =
    Unmarshaller.strict[String, LocalDate] { str =>
      LocalDate.parse(str, TimeUtils.formatterDate)
    }

  implicit def localTimeFromStringUnmarshaller: FromStringUnmarshaller[LocalTime] =
    Unmarshaller.strict[String, LocalTime] { str =>
      LocalTime.parse(str, TimeUtils.formatterTime)
    }

  implicit def localDateTimeFromStringUnmarshaller: FromStringUnmarshaller[LocalDateTime] =
    Unmarshaller.strict[String, LocalDateTime] { str =>
      LocalDateTime.parse(str, TimeUtils.formatterDateTime)
    }

  def ObjectIdPath: PathMatcher1[ObjectId] =
    PathMatcher("""[\da-fA-F]{24}""".r).flatMap { string =>
      try ObjectId.parse(string).toOption
      catch {
        case _: IllegalArgumentException => None
      }
    }

  def ObjectIdSegment: PathMatcher1[String] =
    PathMatcher("""[\da-fA-F]{24}""".r).flatMap { string =>
      Some(string).filter(ObjectId.isValid)
    }

  def hsLogRequest(logger: com.typesafe.scalalogging.Logger): Directive0 = mapRequest { req =>
    def entity = req.entity match {
      case HttpEntity.Empty => ""
      case _                => "\n" + req.entity
    }

    logger.debug(s"""
         |method: ${req.method.value}
         |uri: ${req.uri}
         |search: ${req.uri.rawQueryString}
         |header: ${req.headers.mkString("\n        ")}$entity""".stripMargin)
    req
  }

  def setNoCache: Directive0 =
    mapResponseHeaders(
      h => h ++ List(headers.`Cache-Control`(`no-store`, `no-cache`), headers.RawHeader("Pragma", "no-cache")))

  def completeOk: Route = complete(HttpEntity.Empty)

  def completeNotImplemented: Route = complete(StatusCodes.NotImplemented)

  def pathGet[L](pm: PathMatcher[L]): Directive[L] = path(pm) & get

  def pathPost[L](pm: PathMatcher[L]): Directive[L] = path(pm) & post

  def pathPut[L](pm: PathMatcher[L]): Directive[L] = path(pm) & put

  def pathDelete[L](pm: PathMatcher[L]): Directive[L] = path(pm) & delete

  def putEntity[T](um: FromRequestUnmarshaller[T]): Directive1[T] = put & entity(um)

  def postEntity[T](um: FromRequestUnmarshaller[T]): Directive1[T] = post & entity(um)

  def completionStageComplete(
      future: java.util.concurrent.CompletionStage[AnyRef],
      needContainer: Boolean = false,
      successCode: StatusCode = StatusCodes.OK): Route = {
    import scala.compat.java8.FutureConverters._
    val f: AnyRef => Route = objectComplete(_, needContainer, successCode)
    onSuccess(future.toScala).apply(f)
  }

  def futureComplete(
      future: Future[AnyRef],
      needContainer: Boolean = false,
      successCode: StatusCode = StatusCodes.OK): Route = {
    val f: AnyRef => Route = objectComplete(_, needContainer, successCode)
    onSuccess(future).apply(f)
  }

  @tailrec
  final def objectComplete(obj: Any, needContainer: Boolean = false, successCode: StatusCode = StatusCodes.OK): Route =
    obj match {
      case Right(result) =>
        objectComplete(result, needContainer, successCode)

      case Left(e: HSException) =>
        objectComplete(e, needContainer, successCode)

      case Some(result) =>
        objectComplete(result, needContainer, successCode)

      case None =>
        complete(HSNotFoundException("数据不存在"))

      case response: HttpResponse =>
        complete(response)

      case responseEntity: ResponseEntity =>
        complete(HttpResponse(successCode, entity = responseEntity))

      case result: ApiResult =>
        import helloscala.http.JacksonSupport._
        val status =
          if (result.errCode == 0) StatusCodes.OK
          else if (successCode != StatusCodes.OK) successCode
          else StatusCodes.getForKey(result.errCode).getOrElse(StatusCodes.OK)
        complete((status, result))

      case status: StatusCode =>
        complete(status)

      case result =>
        import helloscala.http.JacksonSupport._
        val resp = if (needContainer) ApiResult(0, data = Some(result)) else result
        complete((successCode, resp))
    }

  def eitherComplete[T](either: Either[HSException, T]): Route = either match {
    case Right(result) => objectComplete(result)
    case Left(e)       => objectComplete(e)
  }

  def multiUploadedFile: Directive1[immutable.Seq[(FileInfo, Path)]] =
    entity(as[Multipart.FormData])
      .flatMap { formData =>
        extractRequestContext.flatMap { ctx =>
          import ctx.executionContext
          import ctx.materializer

          val multiPartF = formData.parts
            .map { part =>
              val destination = Files.createTempFile("akka-http-upload", ".tmp")
              val uploadedF: Future[(FileInfo, Path)] =
                part.entity.dataBytes
                  .runWith(FileIO.toPath(destination))
                  .map(_ => (FileInfo(part.name, part.filename.get, part.entity.contentType), destination))
              uploadedF
            }
            .runWith(Sink.seq)
            .flatMap(list => Future.sequence(list))

          onSuccess(multiPartF)
        }
      }
      .flatMap {
        case Nil  => reject(ValidationRejection("没有任何上传文件"))
        case list => provide(list)
      }

  def multiFileUpload: Directive1[immutable.Seq[(FileInfo, Source[ByteString, Any])]] =
    entity(as[Multipart.FormData])
      .flatMap { formData =>
        extractRequestContext.flatMap { ctx =>
          import ctx.materializer

          val multiPartF = formData.parts
            .map(part => (FileInfo(part.name, part.filename.get, part.entity.contentType), part.entity.dataBytes))
            .runWith(Sink.seq)

          onSuccess(multiPartF)
        }
      }
      .flatMap {
        case Nil  => reject(ValidationRejection("没有任何上传文件"))
        case list => provide(list)
      }

  /**
   * REST API 转发代理
   *
   * @param uri 要转发的地址
   * @param sourceQueue AkkaHTTP 源连接队列
   * @return
   */
  def restApiProxy(uri: Uri)(implicit sourceQueue: AkkaHttpSourceQueue): Route =
    extractRequestContext { ctx =>
      val req = ctx.request
      val request = req.copy(uri = uri.withQuery(req.uri.query()))
      val future = HttpUtils.hostRequest(request)(sourceQueue.httpSourceQueue, ctx.executionContext)
      onSuccess(future) { response =>
        complete(response)
      }
    }

  def notPathPrefixTest[L](pm: PathMatcher[L]): Directive0 =
    rawNotPathPrefixTest(Slash ~ pm)

  def rawNotPathPrefixTest[L](pm: PathMatcher[L]): Directive0 = {
    implicit val LIsTuple: Tuple[L] = pm.ev
    extract(ctx => pm(ctx.unmatchedPath)).flatMap {
      case Matched(v, values) =>
        reject
      case Unmatched =>
        pass
    }
  }

}
