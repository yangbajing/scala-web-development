package helloscala.common.data

case class ApiResult(errCode: Int, errMsg: String = "", data: Option[Any] = None)
