package scalaweb.auth.web.route

import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import helloscala.http.route.AbstractRoute
import message.oauth.AuthorizeSigninRequest
import message.oauth.AuthorizeTokenRequest
import message.oauth.GrantType
import scalaweb.auth.model.AuthRejection
import scalaweb.auth.service.AuthService

class AuthRoute(val authService: AuthService)
    extends AbstractRoute
    with AuthDirectives
    with StrictLogging {
  override def route: Route =
    pathPrefix("auth") {
      authorizeSigninHTML ~
      signinRoute ~
      generateTokenRoute ~
      checkTokenRoute ~
      refreshTokenRoute ~
      callbackRoute
    } ~
    htmlRoute

  val authorizePDM =
    (
      'account,
      'password,
      'response_type,
      'client_id,
      'redirect_uri,
      'scope,
      'state.?,
      'access_type.?,
      'login_hint.?)

  def authorizeSigninHTML: Route = pathGet("authorize") {
    getFromDirectory("oauth/web/auth/authorize.html")
//    getFromResource("html/auth/authorize.html")
  }

  def signinRoute: Route = pathPost("signin") {
    formFields(authorizePDM).as(AuthorizeSigninRequest.apply _) { req =>
      onSuccess(authService.authorizeSignin(req)) { redirectUri =>
        complete(
          HttpResponse(StatusCodes.Found, headers = List(Location(redirectUri))))
      }
    }
  }

  val tokenRequestPDM = (
    'grant_type.as[GrantType],
    'client_id,
    'client_secret.?,
    'code.?,
    'redirect_uri.?,
    'echostr.?,
    'refresh_token.?,
    'username.?,
    'password.?)

  def generateTokenRoute: Route = path("token" | "access_token") {
    get {
      parameters(tokenRequestPDM).as(AuthorizeTokenRequest.apply _) { req =>
        extractExecutionContext { implicit ec =>
          futureComplete(authService.generateToken(req))
        }
      }
    } ~
    post {
      formFields(tokenRequestPDM).as(AuthorizeTokenRequest.apply _) { req =>
        extractExecutionContext { implicit ec =>
          futureComplete(authService.generateToken(req))
        }
      }
    }
  }

  def checkTokenRoute: Route = pathGet("check_token") {
    optionalAccessToken {
      case Some(accessToken) =>
        onSuccess(authService.checkToken(accessToken)) {
          case true => complete(StatusCodes.OK)
          case _    => complete(StatusCodes.Unauthorized)
        }
      case _ => reject(AuthRejection("参数'access_token'缺失"))
    }
  }

  def refreshTokenRoute: Route = pathGet("refresh_token" / Segment) {
    refreshToken =>
      futureComplete(authService.refreshToken(refreshToken))
  }

  def callbackRoute: Route = pathGet("callback") {
    parameters(('code, 'state.?(""))) { (code, state) =>
      futureComplete(authService.authCallback(code, state))
    }
  }

  def htmlRoute: Route = //getFromResourceDirectory("html")
    getFromDirectory("oauth/web")
}
