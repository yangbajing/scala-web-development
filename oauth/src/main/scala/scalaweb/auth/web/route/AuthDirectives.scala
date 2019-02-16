package scalaweb.auth.web.route

import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.directives.FormFieldDirectives.FieldDef.{extractField, filter, stringFromStrictForm}
import akka.http.scaladsl.server.directives.FormFieldDirectives.FieldDefAux
import helloscala.http.route.AbstractRoute
import message.oauth.{AccessSession, GrantType}
import scalaweb.auth.model.AuthRejection
import scalaweb.auth.service.AuthService

trait AuthDirectives { this: AbstractRoute =>
  val authService: AuthService

  def optionalAccessToken: Directive1[Option[String]] = extract { ctx =>
    val authorization = ctx.request.header[Authorization].flatMap { header =>
      header.credentials match {
        case OAuth2BearerToken(accessToken) => Some(accessToken)
        case _                              => None
      }
    }
    authorization orElse
      ctx.request.uri.query().get(GrantType.access_token.name) orElse
      ctx.request.headers.find(_.lowercaseName() == GrantType.access_token.name).map(_.value())
  }

  def extractAccessSession: Directive1[AccessSession] =
    optionalAccessToken.flatMap {
      case Some(accessToken) =>
        onSuccess(authService.getAccessSession(accessToken)).flatMap(accessOwner => provide(accessOwner))
      case None => reject(AuthRejection("access_token参数必传"))
    }

}
