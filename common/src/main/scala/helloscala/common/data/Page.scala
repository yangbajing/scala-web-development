package helloscala.common.data

import java.time.{LocalDate, LocalDateTime, LocalTime, ZonedDateTime}
import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import helloscala.common.util.{StringUtils, TimeUtils}

import scala.beans.BeanProperty
import scala.util.{Failure, Success, Try}

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-28.
 */
trait PageSort {}

object Page {

  /**
   * 默认页码
   */
  val DEFAULT_PAGE = 1

  /**
   * 默认返回记录条数
   */
  val DEFAULT_SIZE = 10
}

trait Page {
  def page: Int

  def size: Int

  def offset: Int = if (page < 2) 0 else (page - 1) * size
}

trait Pager[T] {
  def lastId: Option[T]

  def size: Int

  def params: Map[String, Any]
}

case class PageInput(@BeanProperty page: Int, @BeanProperty size: Int, @BeanProperty params: Map[String, Any] = Map())
    extends Page
    with StrictLogging {

  def toParamSeq: List[(String, Any)] =
    ("page", page) :: ("size", size) :: params.toList

  def toParamStringSeq: List[(String, String)] = toParamSeq.map {
    case (name, value) => (name, value.toString)
  }

  def getUUID(key: String): Option[UUID] =
    getString(key).flatMap(str => Try(UUID.fromString(str)).toOption)

  def getLocalDate(key: String): Option[LocalDate] =
    getString(key).flatMap(
      str =>
        Try(TimeUtils.toLocalDate(str)) match {
          case Success(v) =>
            Some(v)
          case Failure(e) =>
            logger.warn(s"getLocalDate($key) error: ${e.getMessage}", e)
            None
      }
    )

  def getLocalTime(key: String): Option[LocalTime] =
    getString(key).flatMap(
      str =>
        Try(TimeUtils.toLocalTime(str)) match {
          case Success(v) =>
            Some(v)
          case Failure(e) =>
            logger.warn(s"getLocalTime($key) error: ${e.getMessage}", e)
            None
      }
    )

  def getLocalDateTime(key: String): Option[LocalDateTime] =
    getString(key).flatMap(
      str =>
        Try(TimeUtils.toLocalDateTime(str)) match {
          case Success(v) =>
            Some(v)
          case Failure(e) =>
            logger.warn(s"getLocalDateTime($key) error: ${e.getMessage}", e)
            None
      }
    )

  def getZonedDateTime(key: String): Option[ZonedDateTime] =
    getString(key).flatMap(
      str =>
        Try(TimeUtils.toZonedDateTime(str)) match {
          case Success(v) =>
            Some(v)
          case Failure(e) =>
            logger.warn(s"getZonedDateTime($key) error: ${e.getMessage}", e)
            None
      }
    )

  def getDouble(key: String): Option[Double] =
    params
      .get(key)
      .flatMap {
        case d: Double => Some(d)
        case s: String => Try(s.toDouble).toOption
        case v         => None
      }

  def getBoolean(key: String): Option[Boolean] =
    params
      .get(key)
      .flatMap {
        case b: Boolean => Some(b)
        case s: String  => Try(s.toBoolean).toOption
        case _          => None
      }

  def getInt(key: String): Option[Int] =
    params
      .get(key)
      .flatMap {
        case i: Int    => Some(i)
        case l: Long   => Some(l.toInt)
        case s: String => Try(s.toInt).toOption
        case _         => None
      }

  def getLong(key: String): Option[Long] =
    params
      .get(key)
      .flatMap {
        case l: Long   => Some(l)
        case i: Int    => Some(i.toLong)
        case s: String => Try(s.toLong).toOption
        case _         => None
      }

  def getString(key: String): Option[String] =
    params
      .get(key)
      .flatMap {
        case s: String if StringUtils.isNoneBlank(s) => Some(s)
        case _                                       => None
      }

}

object PageInput {

  val default = PageInput(Page.DEFAULT_PAGE, Page.DEFAULT_SIZE)

  def apply(params: Map[String, Any]): PageInput =
    PageInput(Page.DEFAULT_SIZE, Page.DEFAULT_SIZE, params)

}

trait PageResult[T] extends Page {
  def content: Seq[T]

  def totalElements: Long

  def totalPages: Int = ((totalElements + size) / size).toInt
}

object PageResult {

  def apply[T](page: Int, size: Int, content: Seq[T], totalElements: Int): PageResult[T] =
    DefaultPageResult(page, size, content, totalElements)

}

case class DefaultPageResult[T](
    @BeanProperty page: Int,
    @BeanProperty size: Int,
    @BeanProperty content: Seq[T],
    @BeanProperty totalElements: Long)
    extends PageResult[T] {

  override def toString =
    s"DefaultPageResult($page, $size, $content, $totalElements, $totalPages)"
}
