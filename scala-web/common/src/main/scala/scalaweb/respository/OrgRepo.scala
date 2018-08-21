package scalaweb.respository

import helloscala.common.exception.HSNotFoundException
import scalaweb.model.{OrgCreateReq, OrgPageReq, OrgPageResp}
import scalaweb.respository.SlickProfile.api._

import scala.concurrent.ExecutionContext

object OrgRepo {

  def page(req: OrgPageReq)(implicit ec: ExecutionContext) =
    for {
      content <- tOrgFilter(req).sortBy(_.updatedAt.desc).drop(req.offset).take(req.size).result
      totalElements <- tOrgFilter(req).length.result
    } yield OrgPageResp(content, totalElements, req.page, req.size)

  def tOrgFilter(req: OrgPageReq) =
    tOrg.filter(
      t =>
        dynamicFilter(
          List(
            req.code.map(code => t.code === code),
            req.name.map(name => t.name.? like s"%$name%"),
            req.status.map(status => t.status.? === status)
          )))

  def getById(orgId: Int) = tOrg.filter(_.id === orgId).result.headOption

  def create(req: OrgCreateReq)(implicit ec: ExecutionContext) =
    req.parent
      .map(tOrgMapParents)
      .getOrElse(DBIO.successful(Nil))
      .flatMap(parents => tOrg.returning(tOrg) += req.toOrg(parents))

  def tOrgMapParents(parent: Int)(implicit ec: ExecutionContext) =
    tOrg.filter(_.id === parent).map(_.parents).result.headOption.flatMap {
      case Some(parents) => DBIO.successful(parents)
      case None          => DBIO.failed(HSNotFoundException(s"父节点未找到，id: $parent"))
    }

  def removeByIds(orgIds: Iterable[Int]) = tOrg.filter(_.id inSet orgIds).delete

  def tOrg = TableQuery[TableOrg]

}
