package scalaweb.oauth2.model

import akka.http.scaladsl.server.RejectionWithOptionalCause

case class OAuthRejection(message: String, cause: Option[Throwable] = None)
    extends akka.http.javadsl.server.AuthorizationFailedRejection
    with RejectionWithOptionalCause
