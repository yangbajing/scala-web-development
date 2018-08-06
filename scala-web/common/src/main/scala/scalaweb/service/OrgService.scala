package scalaweb.service

import scalaweb.model.{Org, OrgCreateReq}
import scalaweb.respository.{OrgRepository, Schema}

import scala.concurrent.Future

class OrgService(schema: Schema) {

  private val orgRepository= new OrgRepository(schema)

  def create(req: OrgCreateReq): Future[Org] = {
    ???
  }

}
