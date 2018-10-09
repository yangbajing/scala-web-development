package scalaweb.model

import java.time.OffsetDateTime

import com.fasterxml.jackson.databind.node.ObjectNode
import helloscala.common.data.{Page, PageResult}
import helloscala.common.json.Jackson

case class Org(
    id: Int,
    code: Option[String],
    name: String,
    contact: ObjectNode,
    parent: Option[Int],
    parents: List[Int],
    status: Int = 1,
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    updatedAt: Option[OffsetDateTime] = None
)

case class OrgCreateReq(
    code: Option[String],
    name: String,
    contact: Option[ObjectNode],
    parent: Option[Int]
) {
  def toOrg(parents: List[Int]) = Org(-1, code, name, contact.getOrElse(Jackson.createObjectNode), parent, parents)
}

case class OrgUpdateReq(
    code: Option[String],
    name: Option[String],
    contact: Option[ObjectNode],
    status: Option[Int]
)

case class OrgPageReq(
    code: Option[String],
    name: Option[String],
    status: Option[Int],
    page: Int,
    size: Int
) extends Page

case class OrgPageResp(
    content: Seq[Org],
    totalElements: Long,
    page: Int,
    size: Int
) extends PageResult[Org]
