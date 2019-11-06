package scalaweb.auth.service

import java.net.URLDecoder
import java.time.Duration

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.exception.HSNotImplementedException
import helloscala.common.json.Jackson
import message.oauth._
import pdi.jwt.Jwt
import pdi.jwt.JwtAlgorithm
import pdi.jwt.JwtClaim
import pdi.jwt.JwtHeader

import scala.concurrent.Future

class AuthService(implicit system: ActorSystem) extends StrictLogging {
  import system.dispatcher
  implicit val mat = Materializer(system)

  /**
   * 刷新访问领牌
   * @param rt refresh_token
   * @return [[AccessToken]]
   */
  def refreshToken(rt: String): Future[AccessToken] = ???

  /**
   * 检查访问领牌是否有效
   * @param accessToken 访问令牌
   * @return true：有效，false：无效
   */
  def checkToken(accessToken: String): Future[Boolean] = ???

  def generateToken(req: AuthorizeTokenRequest): Future[AccessToken] =
    req.grant_type match {
      case GrantType.authorization_code => accessTokenForAuthorization(req)
      case GrantType.client_credentials => accessTokenForClient(req)
      case _                            => Future.failed(HSNotImplementedException(s"不支持的授权类型：${req.grant_type}"))
    }

  def accessTokenForClient(req: AuthorizeTokenRequest): Future[AccessToken] =
    Future.failed(HSNotImplementedException(s"暂未实现，只支持${GrantType.client_credentials}模式"))

  val privateKey = """-----BEGIN RSA PRIVATE KEY-----
                           |MIICWwIBAAKBgQDdlatRjRjogo3WojgGHFHYLugdUWAY9iR3fy4arWNA1KoS8kVw
                           |33cJibXr8bvwUAUparCwlvdbH6dvEOfou0/gCFQsHUfQrSDv+MuSUMAe8jzKE4qW
                           |+jK+xQU9a03GUnKHkkle+Q0pX/g6jXZ7r1/xAK5Do2kQ+X5xK9cipRgEKwIDAQAB
                           |AoGAD+onAtVye4ic7VR7V50DF9bOnwRwNXrARcDhq9LWNRrRGElESYYTQ6EbatXS
                           |3MCyjjX2eMhu/aF5YhXBwkppwxg+EOmXeh+MzL7Zh284OuPbkglAaGhV9bb6/5Cp
                           |uGb1esyPbYW+Ty2PC0GSZfIXkXs76jXAu9TOBvD0ybc2YlkCQQDywg2R/7t3Q2OE
                           |2+yo382CLJdrlSLVROWKwb4tb2PjhY4XAwV8d1vy0RenxTB+K5Mu57uVSTHtrMK0
                           |GAtFr833AkEA6avx20OHo61Yela/4k5kQDtjEf1N0LfI+BcWZtxsS3jDM3i1Hp0K
                           |Su5rsCPb8acJo5RO26gGVrfAsDcIXKC+bQJAZZ2XIpsitLyPpuiMOvBbzPavd4gY
                           |6Z8KWrfYzJoI/Q9FuBo6rKwl4BFoToD7WIUS+hpkagwWiz+6zLoX1dbOZwJACmH5
                           |fSSjAkLRi54PKJ8TFUeOP15h9sQzydI8zJU+upvDEKZsZc/UhT/SySDOxQ4G/523
                           |Y0sz/OZtSWcol/UMgQJALesy++GdvoIDLfJX5GBQpuFgFenRiRDabxrE9MNUZ2aP
                           |FaFp+DyAe+b4nDwuJaW2LURbr8AEZga7oQj0uYxcYw==
                           |-----END RSA PRIVATE KEY-----""".stripMargin

  def accessTokenForAuthorization(req: AuthorizeTokenRequest): Future[AccessToken] =
    Future {
      val accessToken = Jwt.encode(JwtHeader(JwtAlgorithm.RS256), JwtClaim(), privateKey)
      AccessToken(accessToken, Duration.ofHours(2).getSeconds)
    }

  def authorizeSignin(req: AuthorizeSigninRequest): Future[String] = Future {
    logger.debug(Jackson.stringify(req))
    val code = "code"
    val redirectUri = List("code" -> code, "state" -> req.state.getOrElse(""))
      .filter(_._2.nonEmpty)
      .map { case (name, value) => s"$name=$value" }
      .mkString(s"${URLDecoder.decode(req.redirect_uri, "UTF-8")}?", "&", "")
    logger.debug("redirectUri: " + redirectUri)
    redirectUri
  }

  def getAccessSession(accessToken: String): Future[AccessSession] = ???

  def authCallback(code: String, state: String): Future[AccessToken] = {
    import helloscala.http.JacksonSupport._
    val request = HttpRequest(
      HttpMethods.POST,
      "http://localhost:33333/auth/token",
      entity = FormData(
        "grant_type" -> GrantType.authorization_code.name,
        "client_id" -> "111111",
        "client_secret" -> "111111",
        "code" -> code,
        "redirect_uri" -> "http://localhost:33333/auth/callback").toEntity)
    Http().singleRequest(request).flatMap(response => Unmarshal(response.entity).to[AccessToken])
  }

}
