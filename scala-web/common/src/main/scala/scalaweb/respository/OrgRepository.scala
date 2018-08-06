package scalaweb.respository

import scala.concurrent.Future

class OrgRepository(schema: Schema) {

  import schema.profile.api._
  import schema._

  def removeByIds(orgIds: Iterable[Int]): Future[Array[Int]] = {
???
  }

}
