package helloscala.common.exception

import helloscala.common.IntStatus

class HSException(val errCode: Int, val errMsg: String, val cause: Throwable) extends RuntimeException(errMsg, cause) {
  def this(errMsg: String, cause: Throwable) {
    this(IntStatus.INTERNAL_ERROR, errMsg, cause)
  }
  def this(errMsg: String) {
    this(IntStatus.INTERNAL_ERROR, errMsg, null)
  }
}

case class HSAcceptedWarning(
    override val errMsg: String,
    override val errCode: Int = IntStatus.ACCEPTED,
    override val cause: Throwable = null)
    extends HSException(errCode, errMsg, cause)

case class HSBadRequestException(
    override val errMsg: String,
    override val errCode: Int = IntStatus.BAD_REQUEST,
    override val cause: Throwable = null)
    extends HSException(errCode, errMsg, cause)

case class HSUnauthorizedException(
    override val errMsg: String,
    override val errCode: Int = IntStatus.UNAUTHORIZED,
    override val cause: Throwable = null)
    extends HSException(errCode, errMsg, cause)

case class HSNoContentException(
    override val errMsg: String,
    override val errCode: Int = IntStatus.NO_CONTENT,
    override val cause: Throwable = null)
    extends HSException(errCode, errMsg, cause)

case class HSForbiddenException(
    override val errMsg: String,
    override val errCode: Int = IntStatus.FORBIDDEN,
    override val cause: Throwable = null)
    extends HSException(errCode, errMsg, cause)

case class HSNotFoundException(
    override val errMsg: String,
    override val errCode: Int = IntStatus.NOT_FOUND,
    override val cause: Throwable = null)
    extends HSException(errCode, errMsg, cause)

case class HSConflictException(
    override val errMsg: String,
    override val errCode: Int = IntStatus.CONFLICT,
    override val cause: Throwable = null)
    extends HSException(errCode, errMsg, cause)

case class HSNotImplementedException(
    override val errMsg: String,
    override val errCode: Int = IntStatus.NOT_IMPLEMENTED,
    override val cause: Throwable = null)
    extends HSException(errCode, errMsg, cause)

case class HSInternalErrorException(
    override val errMsg: String,
    override val errCode: Int = IntStatus.INTERNAL_ERROR,
    override val cause: Throwable = null)
    extends HSException(errCode, errMsg, cause)
