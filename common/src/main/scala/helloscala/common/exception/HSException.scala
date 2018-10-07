package helloscala.common.exception

import helloscala.common.util.ErrCodes

class HSException(val errCode: Int, val errMsg: String, val cause: Throwable) extends RuntimeException(errMsg, cause)

case class HSAcceptedWarning(
    override val errMsg: String,
    override val errCode: Int = ErrCodes.ACCEPTED,
    override val cause: Throwable = null
) extends HSException(errCode, errMsg, cause)

case class HSBadRequestException(
    override val errMsg: String,
    override val errCode: Int = ErrCodes.BAD_REQUEST,
    override val cause: Throwable = null
) extends HSException(errCode, errMsg, cause)

case class HSUnauthorizedException(
    override val errMsg: String,
    override val errCode: Int = ErrCodes.UNAUTHORIZED,
    override val cause: Throwable = null
) extends HSException(errCode, errMsg, cause)

case class HSNoContentException(
    override val errMsg: String,
    override val errCode: Int = ErrCodes.NO_CONTENT,
    override val cause: Throwable = null
) extends HSException(errCode, errMsg, cause)

case class HSForbiddenException(
    override val errMsg: String,
    override val errCode: Int = ErrCodes.FORBIDDEN,
    override val cause: Throwable = null
) extends HSException(errCode, errMsg, cause)

case class HSNotFoundException(
    override val errMsg: String,
    override val errCode: Int = ErrCodes.NOT_FOUND,
    override val cause: Throwable = null
) extends HSException(errCode, errMsg, cause)

case class HSConflictException(
    override val errMsg: String,
    override val errCode: Int = ErrCodes.CONFLICT,
    override val cause: Throwable = null
) extends HSException(errCode, errMsg, cause)

case class HSNotImplementedException(
    override val errMsg: String,
    override val errCode: Int = ErrCodes.NOT_IMPLEMENTED,
    override val cause: Throwable = null
) extends HSException(errCode, errMsg, cause)

case class HSInternalErrorException(
    override val errMsg: String,
    override val errCode: Int = ErrCodes.INTERNAL_ERROR,
    override val cause: Throwable = null
) extends HSException(errCode, errMsg, cause)
