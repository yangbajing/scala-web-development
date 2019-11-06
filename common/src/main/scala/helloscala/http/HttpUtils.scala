package helloscala.http

import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.time.Instant

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.Materializer
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.QueueOfferResult
import akka.util.ByteString
import com.fasterxml.jackson.databind.node.ArrayNode
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.exception.HSException
import helloscala.common.json.Jackson
import helloscala.common.util.DigestUtils
import helloscala.common.util.StringUtils
import helloscala.common.util.Utils

import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.reflect.ClassTag
import scala.util.Failure
import scala.util.Success

/**
 * Akka HTTP Utils
 *
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-14.
 */
object HttpUtils extends StrictLogging {

  val AKKA_HTTP_ROUTES_DISPATCHER = "akka-http-routes-dispatcher"

  def dump(response: HttpResponse)(implicit mat: Materializer): Unit = {
    val future = Unmarshal(response.entity).to[String]
    val value = Await.result(future, 10.seconds)
    println(s"[$response]\n\t\t$value\n")
  }

  @inline
  def haveSuccess(status: StatusCode): Boolean = haveSuccess(status.intValue())

  @inline
  def haveSuccess(status: Int): Boolean = status >= 200 && status < 300

  def mapHttpResponse[R: ClassTag](response: HttpResponse)(
      implicit mat: Materializer,
      um: FromEntityUnmarshaller[R] = JacksonSupport.unmarshaller,
      ec: ExecutionContext = null): Future[Either[HSException, R]] =
    if (HttpUtils.haveSuccess(response.status)) {
      Unmarshal(response.entity).to[R].map(v => Right(v))(if (ec eq null) mat.executionContext else ec)
    } else {
      mapHttpResponseError[R](response)
    }

  def mapHttpResponseList[R](response: HttpResponse)(
      implicit ev1: ClassTag[R],
      mat: Materializer,
      ec: ExecutionContext = null): Future[Either[HSException, List[R]]] =
    if (HttpUtils.haveSuccess(response.status)) {
      Unmarshal(response.entity)
        .to[ArrayNode](JacksonSupport.unmarshaller, ec, mat)
        .map { array =>
          val list = array.asScala
            .map(node => Jackson.defaultObjectMapper.treeToValue(node, ev1.runtimeClass).asInstanceOf[R])
            .toList
          Right(list)
        }(if (ec eq null) mat.executionContext else ec)
    } else {
      mapHttpResponseError[List[R]](response)
    }

  def mapHttpResponseError[R](
      response: HttpResponse)(implicit mat: Materializer, ec: ExecutionContext = null): Future[Either[HSException, R]] =
    if (response.entity.contentType.mediaType == MediaTypes.`application/json`) {
      Unmarshal(response.entity)
        .to[HSException](JacksonSupport.unmarshaller, ec, mat)
        .map(e => Left(e))(if (ec eq null) mat.executionContext else ec)
    } else {
      Unmarshal(response.entity)
        .to[String]
        .map(errMsg => Left(new HSException(response.status.intValue(), errMsg, null)))(
          if (ec eq null) mat.executionContext else ec)
    }

  def queryToMap(request: HttpRequest): Map[String, String] = queryToMap(request.uri.query())

  def queryToMap(query: Uri.Query): Map[String, String] = query.toMap

  /**
   * 从 HTTP header Content-Type 中获取 charset
   *
   * @param ct HTTP header Content-Type 值
   * @return
   */
  def parseCharset(ct: String): Option[Charset] =
    try {
      if (StringUtils.isNoneBlank(ct) && ct.contains("=")) {
        val arr = ct.split('=')
        val cs = Charset.forName(arr.last)
        Option(cs)
      } else {
        None
      }
    } catch {
      case _: UnsupportedCharsetException =>
        None
    }

  //  private def notTransforHeader(header: HttpHeader) = {
  //    header.lowercaseName().equals("content-type") || header.lowercaseName().equals("content-length")
  //  }

  /**
   * 根据 Content-Type 字符串解析转换成 [[ContentType]]
   *
   * @param value Content-Type 字符串
   * @return
   */
  def parseContentType(value: String): Option[ContentType] = {
    // TODO akka-http 的ContentType/MediaType覆盖不够怎么办？

    var contentType = value
    var charset = ""
    if (StringUtils.isNoneBlank(contentType)) {
      val arr = contentType.split(';')
      contentType = arr(0)
      if (arr.length == 2) {
        val arr2 = arr(1).split('=')
        if (arr2.length == 2)
          charset = arr2(1).trim
      }
    }

    val tupleKey = contentType.split('/') match {
      case Array(k, v) => (k.toLowerCase(), v.toLowerCase())
      case Array(k)    => (k.toLowerCase(), "")
      case _           => ("", "")
    }
    logger.debug(s"tupleKey: $tupleKey")

    if (tupleKey._2.contains("powerpoint")) {
      Some(ContentType(MediaTypes.`application/mspowerpoint`))
    } else if (tupleKey._2.contains("excel")) {
      Some(ContentType(MediaTypes.`application/excel`))
    } else if (tupleKey._2.contains("msword")) {
      Some(ContentType(MediaTypes.`application/msword`))
    } else {
      tupleKeyToContentType(charset, tupleKey)
    }
  }

  private def tupleKeyToContentType(charset: String, tupleKey: (String, String)) = {
    val mediaType = MediaTypes.getForKey(tupleKey).getOrElse(MediaTypes.`application/octet-stream`)
    val httpContentType: ContentType = mediaType match {
      case woc: MediaType.WithOpenCharset =>
        val httpCharset = HttpCharsets.getForKeyCaseInsensitive(charset).getOrElse(HttpCharsets.`UTF-8`)
        woc.withCharset(httpCharset)
      case mt: MediaType.Binary           => ContentType(mt)
      case mt: MediaType.WithFixedCharset => ContentType(mt)
      case _                              => null
    }
    Option(httpContentType)
  }

  def getMediaTypeFromSuffix(suffix: String): Option[MediaType] =
    ???

  def cachedHostConnectionPool(url: String)(implicit mat: Materializer): HttpSourceQueue = {
    val uri = Uri(url)
    uri.scheme match {
      case "http"  => cachedHostConnectionPool(uri.authority.host.address(), uri.authority.port)
      case "https" => cachedHostConnectionPoolHttps(uri.authority.host.address(), uri.authority.port)
      case _       => throw new IllegalArgumentException(s"URL: $url 不是有效的 http 或 https 协议")
    }
  }

  /**
   * 获取 CachedHostConnectionPool，当发送的url不包含 host 和 port 时将使用默认值
   *
   * @param host 默认host
   * @param port 默认port
   * @param mat  Materializer
   * @return
   */
  def cachedHostConnectionPool(host: String, port: Int = 80)(implicit mat: Materializer): HttpSourceQueue = {
    implicit val system = mat.system
    val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]](host, port)
    Source
      .queue[(HttpRequest, Promise[HttpResponse])](512, OverflowStrategy.dropNew)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case ((Success(resp), p)) => p.success(resp)
        case ((Failure(e), p))    => p.failure(e)
      }))(Keep.left)
      .run()
  }

  /**
   * 获取 CachedHostConnectionPoolHttps，同[[cachedHostConnectionPool()]]，区别是使用HTTPs协议
   *
   * @param host 默认host
   * @param port 默认port
   * @param mat  Materializer
   * @return
   */
  def cachedHostConnectionPoolHttps(host: String, port: Int = 80)(implicit mat: Materializer): HttpSourceQueue = {
    implicit val system = mat.system
    val poolClientFlow = Http().cachedHostConnectionPoolHttps[Promise[HttpResponse]](host, port)
    Source
      .queue[(HttpRequest, Promise[HttpResponse])](512, OverflowStrategy.dropNew)
      .via(poolClientFlow)
      .toMat(Sink.foreach({
        case ((Success(resp), p)) => p.success(resp)
        case ((Failure(e), p))    => p.failure(e)
      }))(Keep.left)
      .run()
  }

  /**
   * 为 HttpRequest 添加 API Token 请求HTTP Headers
   *
   * @param request Akka HTTP HttpRequest
   * @param appId   app-id
   * @param appKey  app-key
   * @param echoStr 随机字符串
   * @return
   */
  def applyApiToken(
      request: HttpRequest,
      appId: String,
      appKey: String,
      echoStr: String = Utils.randomString(12)): HttpRequest = {
    val timestamp = Instant.now().getEpochSecond.toString
    val echoStr = Utils.randomString(8)
    val accessToken = DigestUtils.sha256Hex(appId + appKey + echoStr + timestamp)
    val headers = List(
      RawHeader(HttpConstants.HS_APP_ID, appId),
      RawHeader(HttpConstants.HS_TIMESTAMP, timestamp),
      RawHeader(HttpConstants.HS_ECHO_STR, echoStr),
      RawHeader(HttpConstants.HS_ACCESS_TOKEN, accessToken))
    request.withHeaders(headers)
  }

  /**
   * 发送 Http 请求
   * @param method 请求方法类型
   * @param uri 请求地址
   * @param params 请求URL查询参数
   * @param data 请求数据（将备序列化成JSON）
   * @param headers 请求头
   * @param protocol HTTP协议版本
   * @return HttpResponse
   */
  def singleRequest(
      method: HttpMethod,
      uri: Uri,
      params: Seq[(String, String)] = Nil,
      data: AnyRef = null,
      headers: immutable.Seq[HttpHeader] = Nil,
      protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`)(implicit mat: Materializer): Future[HttpResponse] = {
    val request =
      HttpRequest(method, uri.withQuery(Uri.Query(uri.query() ++ params: _*)), headers, entity = data match {
        case null                    => HttpEntity.Empty
        case entity: UniversalEntity => entity
        case _                       => HttpEntity(ContentTypes.`application/json`, Jackson.defaultObjectMapper.writeValueAsString(data))
      }, protocol = protocol)
    singleRequest(request)
  }

  /**
   * 发送 Http 请求，使用 [[akka.http.scaladsl.HttpExt.singleRequest]]
   *
   * @param request HttpRequest
   * @param mat Materializer
   * @return
   */
  def singleRequest(request: HttpRequest)(implicit mat: Materializer): Future[HttpResponse] =
    Http()(mat.system).singleRequest(request)

  /**
   * 发送 Http 请求，使用 CachedHostConnectionPool。见：[[cachedHostConnectionPool()]]
   *
   * @param request         HttpRequest
   * @param httpSourceQueue 使用了CachedHostConnectionPool的 HTTP 队列
   * @return Future[HttpResponse]
   */
  def hostRequest(
      request: HttpRequest)(implicit httpSourceQueue: HttpSourceQueue, ec: ExecutionContext): Future[HttpResponse] = {
    val responsePromise = Promise[HttpResponse]()
    httpSourceQueue.offer(request -> responsePromise).flatMap {
      case QueueOfferResult.Enqueued    => responsePromise.future
      case QueueOfferResult.Dropped     => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
      case QueueOfferResult.Failure(ex) => Future.failed(ex)
      case QueueOfferResult.QueueClosed =>
        Future.failed(
          new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
    }
  }

  def hostRequest(
      method: HttpMethod,
      uri: Uri,
      params: Seq[(String, String)] = Nil,
      data: AnyRef = null,
      headers: immutable.Seq[HttpHeader] = Nil)(
      implicit httpSourceQueue: HttpSourceQueue,
      ec: ExecutionContext): Future[HttpResponse] = {
    val entity = if (data != null) {
      data match {
        case entity: MessageEntity => entity
        case _                     => HttpEntity(ContentTypes.`application/json`, Jackson.defaultObjectMapper.writeValueAsString(data))
      }
    } else {
      HttpEntity.Empty
    }
    hostRequest(HttpRequest(method, uri.withQuery(Uri.Query(uri.query() ++ params: _*)), headers, entity))
  }

  def makeRequest(
      method: HttpMethod,
      uri: Uri,
      params: Seq[(String, Any)] = Nil,
      data: AnyRef = null,
      headers: immutable.Seq[HttpHeader] = Nil): HttpRequest = {
    val entity = if (data != null) {
      data match {
        case entity: MessageEntity => entity
        case _                     => HttpEntity(ContentTypes.`application/json`, Jackson.defaultObjectMapper.writeValueAsString(data))
      }
    } else {
      HttpEntity.Empty
    }

    HttpRequest(
      method,
      uri.withQuery(Uri.Query(params.map { case (key, value) => key -> value.toString }: _*)),
      headers,
      entity)
  }

  def toStrictEntity(response: HttpResponse)(implicit mat: Materializer): HttpEntity.Strict =
    toStrictEntity(response.entity)

  def toByteString(response: HttpResponse)(implicit mat: Materializer): Future[ByteString] =
    Unmarshal(response.entity).to[ByteString]

  def toStrictEntity(responseEntity: ResponseEntity)(implicit mat: Materializer): HttpEntity.Strict = {
    import scala.concurrent.duration._
    val dr = 10.seconds
    val f = responseEntity.toStrict(dr)
    Await.result(f, dr)
  }

}
