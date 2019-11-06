package scalaweb.service

import scalaweb.model.Org
import scalaweb.model.OrgCreateReq
import scalaweb.model.OrgPageReq
import scalaweb.model.OrgPageResp
import scalaweb.respository.OrgRepo
import scalaweb.respository.Schema

import scala.concurrent.Future

class OrgService(schema: Schema) {
  import schema._

  def page(req: OrgPageReq): Future[OrgPageResp] = run(OrgRepo.page(req))

  def getById(orgId: Int): Future[Option[Org]] = run(OrgRepo.getById(orgId))

  def create(req: OrgCreateReq): Future[Org] = runTransaction(OrgRepo.create(req))

  def removeByIds(ids: Iterable[Int]): Future[Int] = runTransaction(OrgRepo.removeByIds(ids))

}
