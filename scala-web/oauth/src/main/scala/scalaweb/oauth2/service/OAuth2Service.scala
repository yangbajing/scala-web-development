package scalaweb.oauth2.service

import helloscala.common.exception.HSNotImplementedException
import message.oauth.{AccessSession, AccessToken, AuthorizeSigninRequest, AuthorizeTokenRequest}
import scalaweb.oauth2.OAuthConstants

import scala.concurrent.Future

class OAuth2Service {
  def validationAccessToken(accessToken: String): Future[Boolean] = ???

  def authorizeSignin(req: AuthorizeSigninRequest): Future[String] = ???

  def getAccessSession(accessToken: String): Future[AccessSession] = ???

  def accessTokenForClient(req: AuthorizeTokenRequest): Future[AccessToken] =
    Future.failed(HSNotImplementedException(s"暂未实现，只支持${OAuthConstants.AUTHORIZATION_CODE}模式"))

  def accessTokenForAuthorization(req: AuthorizeTokenRequest): Future[AccessToken] = ???

}
