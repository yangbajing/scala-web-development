package helloscala.http

import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.ContentTypeRange
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.util.ByteString
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import helloscala.common.json.Jackson

import scala.collection.immutable.Seq
import scala.reflect.runtime.universe._

object JacksonSupport extends JacksonSupport {

  def getJsonNode(content: String)(implicit objectMapper: ObjectMapper = Jackson.defaultObjectMapper): JsonNode =
    objectMapper.readTree(content)

  def isError(content: String)(implicit objectMapper: ObjectMapper = Jackson.defaultObjectMapper): Boolean =
    !isSuccess(content)

  def isSuccess(content: String)(implicit objectMapper: ObjectMapper = Jackson.defaultObjectMapper): Boolean = {
    val node = getJsonNode(content)
    val errCode = node.get("errCode")
    if (errCode eq null) true else errCode.asInt(0) == 0
  }
}

/**
 * JSON marshalling/unmarshalling using an in-scope Jackson's ObjectMapper
 */
trait JacksonSupport {

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    List(`application/json`)

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller.forContentTypes(unmarshallerContentTypes: _*).mapWithCharset {
      case (ByteString.empty, _) => throw Unmarshaller.NoContentException
      case (data, charset)       => data.decodeString(charset.nioCharset.name)
    }

  /**
   * HTTP entity => `A`
   */
  implicit def unmarshaller[A](
      implicit ct: TypeTag[A],
      objectMapper: ObjectMapper = Jackson.defaultObjectMapper): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(data => objectMapper.readValue(data, Jackson.typeReference[A]).asInstanceOf[A])

  /**
   * `A` => HTTP entity
   */
  implicit def marshaller[Object](
      implicit objectMapper: ObjectMapper = Jackson.defaultObjectMapper): ToEntityMarshaller[Object] =
    Marshaller.withFixedContentType(`application/json`)(value =>
      HttpEntity(`application/json`, Jackson.defaultObjectMapper.writeValueAsString(value)))

}
