package scalaweb.data.json.jackson

import java.time.OffsetDateTime

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import helloscala.test.HelloscalaSpec

import scala.collection.immutable

case class Foo(name: String, since: Int)

case class FooTime(name: String, since: OffsetDateTime)

class JacksonSupportTest extends HelloscalaSpec with ScalatestRouteTest {

  // #default-ObjectMapper
  "Default ObjectMapper" should {
    import de.heikoseeberger.akkahttpjackson.JacksonSupport._

    "从case class序列化和反序列化" in {
      val foo = Foo("bar", 2018)
      val result = Marshal(foo).to[RequestEntity].flatMap(Unmarshal(_).to[Foo]).futureValue
      foo shouldBe result
    }

    "从数组case class序列化和反序列化" in {
      val foos = Seq(Foo("bar", 2018))
      val result = Marshal(foos).to[RequestEntity].flatMap(Unmarshal(_).to[Seq[Foo]]).futureValue
      foos shouldBe result
    }

    "不支持OffsetDateTime" in {
      val foo = FooTime("羊八井", OffsetDateTime.now())
      val requestEntity = Marshal(foo).to[RequestEntity].futureValue
      intercept[MismatchedInputException] {
        throw Unmarshal(requestEntity).to[Foo].failed.futureValue
      }
    }
  }
  // #default-ObjectMapper

  // #custom-ObjectMapper
  "Custom ObjectMapper" should {
    import de.heikoseeberger.akkahttpjackson.JacksonSupport._
    implicit val objectMapper: ObjectMapper = helloscala.common.json.Jackson.defaultObjectMapper

    "支持OffsetDateTime" in {
      val foo = FooTime("羊八井", OffsetDateTime.now())
      val requestEntity = Marshal(foo).to[RequestEntity].futureValue
      val result = Unmarshal(requestEntity).to[FooTime].futureValue
      foo shouldBe result
    }

    "从数组case class序列化和反序列化" in {
      val foos = Seq(FooTime("羊八井", OffsetDateTime.now()))
      val results = Marshal(foos).to[RequestEntity].flatMap(Unmarshal(_).to[Seq[FooTime]]).futureValue
      foos shouldBe results
    }
  }
  // #custom-ObjectMapper

  // #custom-unmarshallerContentTypes
  "Custom unmarshallerContentTypes" should {
    final object CustomJacksonSupport extends JacksonSupport {
      override def unmarshallerContentTypes: immutable.Seq[ContentTypeRange] =
        List(MediaTypes.`text/plain`, MediaTypes.`application/json`)
    }

    "text/plain unmarshal failed" in {
      import de.heikoseeberger.akkahttpjackson.JacksonSupport._
      val entity = HttpEntity("""{"name": "羊八井", "since": 2018}""")
      entity.contentType.mediaType shouldBe MediaTypes.`text/plain`
      intercept[UnsupportedContentTypeException] {
        throw Unmarshal(entity).to[Foo].failed.futureValue
      }
    }

    "text/plain unmarshal" in {
      import CustomJacksonSupport._
      val entity = HttpEntity("""{"name": "羊八井", "since": 2018}""")
      entity.contentType.mediaType shouldBe MediaTypes.`text/plain`
      val foo = Unmarshal(entity).to[Foo].futureValue
      foo shouldBe Foo("羊八井", 2018)
    }
  }
  // #custom-unmarshallerContentTypes

  // #routing-dsl
  "routing-dsl" should {
    import akka.http.scaladsl.server.Directives._
    import de.heikoseeberger.akkahttpjackson.JacksonSupport._
    implicit val objectMapper: ObjectMapper = helloscala.common.json.Jackson.defaultObjectMapper

    val route: Route = path("api") {
      post {
        entity(as[FooTime]) { foo =>
          complete(foo.copy(since = foo.since.plusYears(1)))
        }
      }
    }

    "post json" in {
      val foo = FooTime("羊八井", OffsetDateTime.now())
      Post("/api", foo) ~> route ~> check {
        status shouldBe StatusCodes.OK
        contentType.mediaType shouldBe MediaTypes.`application/json`
        val payload = responseAs[FooTime]
        foo.name shouldBe payload.name
        foo.since.isBefore(payload.since) shouldBe true
      }
    }
  }
  // #routing-dsl

}
