package akkahttp.json

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import scala.reflect.ClassTag

/**
 * JSON marshalling/unmarshalling using an in-scope Jackson's ObjectMapper
 */
trait JacksonSupport {

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset)       => data.decodeString(charset.nioCharset.name)
      }

  //  private val jsonStringMarshaller = Marshaller.stringMarshaller(MediaTypes.`application/json`)

  /**
   * HTTP entity => `A`
   */
  implicit def unmarshaller[A](
      implicit ct: ClassTag[A],
      objectMapper: ObjectMapper = Jackson.defaultObjectMapper
  ): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(
      data => objectMapper.readValue(data, ct.runtimeClass).asInstanceOf[A]
    )

  /**
   * `A` => HTTP entity
   */
  implicit def marshaller[A](
      implicit objectMapper: ObjectMapper = Jackson.defaultObjectMapper
  ): ToEntityMarshaller[A] =
    //    jsonStringMarshaller.compose(objectMapper.writeValueAsString)
    JacksonHelper.marshaller[A](objectMapper)

}

object JacksonSupport extends JacksonSupport {

  def stringify(value: AnyRef): String =
    Jackson.defaultObjectMapper.writeValueAsString(value)

  def prettyString(value: AnyRef): String = {
    val writer = Jackson.defaultObjectMapper.writer(new DefaultPrettyPrinter())
    writer.writeValueAsString(value)
  }

  def readValue[A](content: String)(
      implicit ct: ClassTag[A],
      objectMapper: ObjectMapper = Jackson.defaultObjectMapper
  ): A =
    objectMapper.readValue(content, ct.runtimeClass).asInstanceOf[A]

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
