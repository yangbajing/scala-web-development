package scalaweb.test.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.BeforeAndAfterAll
import org.scalatest.MustMatchers
import org.scalatest.OptionValues
import org.scalatest.WordSpec
import scalaweb.model.Org
import scalaweb.model.OrgCreateReq
import scalaweb.respository.OrgRepo
import scalaweb.respository.Schema
import scalaweb.service.OrgService

import scala.util.control.NonFatal

class OrgRouteTest
    extends WordSpec
    with BeforeAndAfterAll
    with ScalatestRouteTest
    with MustMatchers
    with OptionValues
    with ScalaFutures {

  private val schema = Schema()
  private var orgIds: Set[Int] = Set()
  private val route: Route = new OrgRoute(new OrgService(schema)).route

  "OrgRoute" should {
    import helloscala.http.JacksonSupport._

    var org: Org = null

    "create" in {
      val req = OrgCreateReq(Some("000001"), "测试组织", None, None)
      Post("/org/item", req) ~> route ~> check {
        status mustBe StatusCodes.Created
        org = responseAs[Org]
        orgIds += org.id
        org.id must be > 0
        org.parent mustBe None
        org.updatedAt mustBe None
      }
    }

    "get" in {
      pending
    }

    "pageRoute" in {
      pending
    }

    "updateRoute" in {
      pending
    }

    "remoteRoute" in {
      pending
    }
  }

  private def cleanup(): Unit =
    try {
      schema.runTransaction(OrgRepo.removeByIds(orgIds)).futureValue
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }

  override def afterAll() {
    cleanup()
    schema.db.close()
    super.afterAll()
  }

}
