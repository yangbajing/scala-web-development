package scalaweb.oauth2.web.route

import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.server.Directive1
import helloscala.http.route.AbstractRoute
import message.oauth.AccessSession
import scalaweb.oauth2.model.OAuthRejection
import scalaweb.oauth2.service.OAuth2Service

trait OAuth2Directives { this: AbstractRoute =>
  val oauth2Service: OAuth2Service

  def optionalAccessToken: Directive1[Option[String]] = extract { ctx =>
    ctx.request.header[Authorization].flatMap { header =>
      header.credentials match {
        case OAuth2BearerToken(accessToken) => Some(accessToken)
        case _                              => None
      }
    } orElse
      ctx.request.uri.query().get("access_token") orElse
      ctx.request.headers.find(_.lowercaseName() == "access_token").map(_.value())
  }

  def extractAccessSession: Directive1[AccessSession] =
    optionalAccessToken.flatMap {
      case Some(accessToken) =>
        onSuccess(oauth2Service.getAccessSession(accessToken)).flatMap { accessOwner =>
          provide(accessOwner)
        }
      case None => reject(OAuthRejection("access_token参数必传"))
    }

}
