package scalaweb.auth.model

import akka.http.scaladsl.server.RejectionWithOptionalCause

case class AuthRejection(message: String, cause: Option[Throwable] = None)
    extends akka.http.javadsl.server.AuthorizationFailedRejection
    with RejectionWithOptionalCause
