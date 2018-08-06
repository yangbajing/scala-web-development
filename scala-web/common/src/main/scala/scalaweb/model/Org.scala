package scalaweb.model

import java.time.OffsetDateTime

case class Org(
    id: Int,
    code: Option[String],
    name: String,
    contact: String,
    parent: Option[Int],
    parents: List[Int],
    status: Int = 1,
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    updatedAt: Option[OffsetDateTime] = None
)

case class OrgCreateReq(
    code: Option[String],
    name: String,
    contact: Option[String]
)

case class OrgUpdateReq(
    code: Option[String],
    name: Option[String],
    contact: Option[String],
    status: Option[Int]
)

case class OrgPageReq(
    code: Option[String],
    name: Option[String],
    status: Option[Int]
)

case class OrgPageResp(
    content: Seq[Org],
    totalElements: Long,
    page: Int,
    size: Int
)
