package scalaweb.oauth2.web.route

import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import helloscala.http.route.AbstractRoute
import message.oauth.{AuthorizeSigninRequest, AuthorizeTokenRequest}
import scalaweb.oauth2.OAuthConstants
import scalaweb.oauth2.model.OAuthRejection
import scalaweb.oauth2.service.OAuth2Service

class OAuth2Route(
    val oauth2Service: OAuth2Service
) extends AbstractRoute
    with OAuth2Directives
    with StrictLogging {

  override def route: Route = pathPrefix("oauth2") {
    authorizeSigninHTML ~
      signinRoute ~
      tokenGetRoute ~
      validationRoute
  }

  def authorizeSigninHTML: Route = pathGet("authorize") {
    getFromResource("html/oauth2/authorize.html")
  }

  def signinRoute: Route = pathPost("signin") {
    val pdm = ('account, 'password, 'response_type, 'client_id, 'redirect_uri, 'scope, 'state)
    formFields(pdm).as(AuthorizeSigninRequest.apply _) { req =>
      onSuccess(oauth2Service.authorizeSignin(req)) { redirectUri =>
        complete(HttpResponse(StatusCodes.Found, headers = List(Location(redirectUri))))
      }
    }
  }

  def tokenGetRoute: Route = (path("token") | path("access_token") & get) {
    val tokenRequestPDM = ('grant_type, 'client_id, 'client_key, 'code, 'redirect_uri, 'echostr)
    parameters(tokenRequestPDM).as(AuthorizeTokenRequest.apply _) { req =>
      extractExecutionContext { implicit ec =>
        val future = req.grantType match {
          case OAuthConstants.AUTHORIZATION_CODE => oauth2Service.accessTokenForAuthorization(req)
          case OAuthConstants.CLIENT_CREDENTIALS => oauth2Service.accessTokenForClient(req)
        }
        futureComplete(future)
      }
    }
  }

  def validationRoute: Route = pathGet("validation") {
    optionalAccessToken {
      case Some(accessToken) =>
        onSuccess(oauth2Service.validationAccessToken(accessToken)) {
          case true => complete(StatusCodes.OK)
          case _    => complete(StatusCodes.Unauthorized)
        }
      case _ => reject(OAuthRejection("参数'access_token'缺失"))
    }
  }

}
