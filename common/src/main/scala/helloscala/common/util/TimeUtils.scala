package helloscala.common.util

import java.sql.{ Date => SQLDate }
import java.sql.{ Time => SQLTime }
import java.sql.{ Timestamp => SQLTimestamp }
import java.time._
import java.time.format.DateTimeFormatter
import java.util.Date

import com.typesafe.scalalogging.StrictLogging

import scala.util.Try

object TimeUtils extends StrictLogging {
  val DATE_TIME_EPOCH: LocalDateTime = LocalDateTime.of(1970, 1, 1, 0, 0, 0)
  val ZONE_CHINA_OFFSET: ZoneOffset = ZoneOffset.ofHours(8)

  val formatterDateTime: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val formatterMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
  val formatterDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  val formatterDateHours: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH")

  val formatterDateMinutes: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  val formatterMinutes: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
  val formatterTime: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

  val DateKeys = List("年", "月", "-", "/", "日")

  @deprecated("直接使用 LocalDateTime.now()", "1.0.1")
  def now(): LocalDateTime = LocalDateTime.now()

  def nowTimestamp(): SQLTimestamp = SQLTimestamp.from(Instant.now())

  /**
   * 函数执行时间
   * @param func 待执行函数
   * @tparam R func函数返回类型
   * @return (func函数返回值，函数执行时间纳秒数)
   */
  def executeTime[R](func: => R): (R, Long) = {
    val begin = System.nanoTime()
    val result = func
    val end = System.nanoTime()
    (result, end - begin)
  }

  /**
   * 函数执行时间
   * @param func 待执行函数
   * @tparam R func函数返回类型
   * @return (func函数返回值，函数执行时间毫秒数)
   */
  def executeMillis[R](func: => R): (R, Long) = {
    val begin = System.currentTimeMillis()
    val result = func
    val end = System.currentTimeMillis()
    (result, end - begin)
  }

  def dumpExecuteTime[R](func: => R): R = {
    val begin = System.currentTimeMillis()
    val result = func
    val duration = Duration.ofMillis(System.currentTimeMillis() - begin)
    logger.info(s"函数执行时间：$duration")
    result
  }

  /**
   * 解析字符串为 LocalDate
   *
   * @param date 字符串形式日期
   * @return 成功返回 LocalDate
   */
  def toLocalDate(date: String): LocalDate =
    Try(LocalDate.parse(date, formatterDate)).getOrElse {
      val (year, month, day) = date.split("""[-/:]""") match {
        case Array(y, m, d) =>
          (y.toInt, m.toInt, d.toInt)
        case Array(y, m) =>
          (y.toInt, m.toInt, 1)
        case Array(y) =>
          (y.toInt, 1, 1)
        case _ =>
          throw new IllegalArgumentException(s"$date is invalid iso date format")
      }

      if (year < 0 || year > 9999)
        throw new IllegalArgumentException(
          s"$date is invalid iso date format ($year)")

      LocalDate.of(year, month, day)
    }

  /**
   * 解析字符串为 LocalTime
   *
   * @param time 字符串形式时间
   * @return 成功返回 LocalTime
   */
  def toLocalTime(time: String): LocalTime =
    Try(LocalTime.parse(time, formatterTime)).getOrElse {
      val (hour, minute, second, nano) =
        time.split("""[:-]""") match {
          case Array(h, m, s) =>
            s.split('.') match {
              case Array(sec, millis) =>
                (h.toInt, m.toInt, sec.toInt, millis.toInt * 1000 * 1000)
              case arr =>
                (h.toInt, m.toInt, arr(0).toInt, 0)
            }
          case Array(h, m) =>
            (h.toInt, m.toInt, 0, 0)
          case Array(h) =>
            (h.toInt, 0, 0, 0)
          case _ =>
            throw new IllegalArgumentException(s"$time is invalid iso time format")
        }

      LocalTime.of(hour, minute, second, nano)
    }

  /**
   * 解析字符串为 LocalDateTime
   *
   * @param date 字符串形式日期
   * @param time 字符串形式时间
   * @return 成功返回 LocalDateTime
   */
  def toLocalDateTime(date: String, time: String): LocalDateTime = {
    val d =
      if (StringUtils.isNoneBlank(date)) toLocalDate(date) else LocalDate.now()
    val t =
      if (StringUtils.isNoneBlank(time)) toLocalTime(time) else LocalTime.now()
    LocalDateTime.of(d, t)
  }

  /**
   * 解析字符串为 LocalDateTime
   *
   * @param ldt 字符串形式日期时间
   * @return 成功返回 LocalDateTime
   */
  def toLocalDateTime(ldt: String): LocalDateTime =
    Try(LocalDateTime.parse(ldt, formatterDateTime)).getOrElse {
      ldt.split("""[ Tt]+""") match {
        case Array(date, time) =>
          toLocalDateTime(date, time)
        case Array(dOrT) =>
          if (containsDateKeys(dOrT)) toLocalDateTime(dOrT, "")
          else toLocalDateTime("", dOrT)
        case _ =>
          throw new DateTimeException(s"$ldt 是无效的日期时间格式，推荐格式：yyyy-MM-dd HH:mm:ss")
      }
    }

  private def containsDateKeys(dOrT: String) =
    DateKeys.exists(v => dOrT.contains(v))

  def toLocalDateTime(instant: Instant): LocalDateTime =
    LocalDateTime.ofInstant(instant, ZONE_CHINA_OFFSET)

  def toLocalDateTime(epochMilli: Long): LocalDateTime =
    toLocalDateTime(Instant.ofEpochMilli(epochMilli))

  def toLocalDateTime(date: Date): LocalDateTime =
    if (date eq null) null
    else toLocalDateTime(date.toInstant)

  def zoneOf(str: String): ZoneId =
    if (str.indexOf('-') >= 0 || str.indexOf('+') >= 0) ZoneOffset.of(str)
    else if (str == "Z") ZoneOffset.UTC
    else ZoneId.of(str)

  def zoneOffsetOf(str: String): ZoneOffset =
    if (str.indexOf('-') >= 0 || str.indexOf('+') >= 0) ZoneOffset.of(str)
    else if (str == "Z") ZoneOffset.UTC
    else ZoneOffset.of(str)

  def toZonedDateTime(zdt: String): ZonedDateTime =
    try {
      zdt.split("""[ Tt]+""") match {
        case Array(date, timezone) =>
          val (time, zone) = timezone.split("""[+-]""") match {
            case Array(timeStr, zoneStr) =>
              (
                timeStr,
                zoneOf((if (timezone.indexOf('-') < 0) '+' else '-') + zoneStr))
            case Array(timeStr) => (timeStr, ZONE_CHINA_OFFSET)
            case _ =>
              throw new DateTimeException(
                s"$zdt 无有效的时区信息，推荐格式：yyyy-MM-dd HH:mm:ss[+Z]")
          }
          toZonedDateTime(date, time, zone)
        case Array(dOrT) =>
          if (containsDateKeys(dOrT)) toZonedDateTime(dOrT, "")
          else toZonedDateTime("", dOrT)
        case _ =>
          throw new DateTimeException(
            s"$zdt 是无效的日期时间格式，推荐格式：yyyy-MM-dd HH:mm:ss[+Z]")
      }
    } catch {
      case e: Exception =>
        logger.warn(s"toZonedDateTime error: $zdt")
        throw e
    }

  def toZonedDateTime(date: String, time: String): ZonedDateTime =
    toZonedDateTime(date, time, ZONE_CHINA_OFFSET)

  def toZonedDateTime(date: String, time: String, zoneId: ZoneId): ZonedDateTime =
    toLocalDateTime(date, time).atZone(zoneId)

  def toOffsetDateTime(zdt: String): OffsetDateTime =
    try {
      zdt.split("""[ Tt]+""") match {
        case Array(date, timezone) =>
          val (time, zone) = timezone.split("""[+-]""") match {
            case Array(timeStr, zoneStr) =>
              (
                timeStr,
                zoneOffsetOf(
                  (if (timezone.indexOf('-') < 0) '+' else '-') + zoneStr))
            case Array(timeStr) => (timeStr, ZONE_CHINA_OFFSET)
            case _ =>
              throw new DateTimeException(
                s"$zdt 无有效的时区信息，推荐格式：yyyy-MM-dd HH:mm:ss[+Z]")
          }
          toOffsetDateTime(date, time, zone)
        case Array(dOrT) =>
          if (containsDateKeys(dOrT)) toOffsetDateTime(dOrT, "")
          else toOffsetDateTime("", dOrT)
        case _ =>
          throw new DateTimeException(
            s"$zdt 是无效的日期时间格式，推荐格式：yyyy-MM-dd HH:mm:ss[+Z]")
      }
    } catch {
      case e: Exception =>
        logger.warn(s"toZonedDateTime error: $zdt")
        throw e
    }

  def toOffsetDateTime(date: String, time: String): OffsetDateTime =
    toOffsetDateTime(date, time, ZONE_CHINA_OFFSET)

  def toOffsetDateTime(
      date: String,
      time: String,
      zoneOffset: ZoneOffset): OffsetDateTime =
    toLocalDateTime(date, time).atOffset(zoneOffset)

  def toDate(ldt: LocalDateTime): Date =
    Date.from(ldt.toInstant(ZONE_CHINA_OFFSET))

  def toDate(zdt: ZonedDateTime): Date = Date.from(zdt.toInstant)

  def toEpochMilli(dt: LocalDateTime): Long =
    dt.toInstant(ZONE_CHINA_OFFSET).toEpochMilli

  def toEpochMilli(dt: String): Long =
    toLocalDateTime(dt).toInstant(ZONE_CHINA_OFFSET).toEpochMilli

  def toSqlTimestamp(dt: LocalDateTime): SQLTimestamp = SQLTimestamp.valueOf(dt)

  def toSqlTimestamp(zdt: ZonedDateTime): SQLTimestamp =
    SQLTimestamp.from(zdt.toInstant)

  def toSqlTimestamp(ist: Instant): SQLTimestamp = SQLTimestamp.from(ist)

  def toSqlDate(date: LocalDate) =
    new SQLDate(toEpochMilli(date.atStartOfDay()))

  def toSqlTime(time: LocalTime) =
    new SQLTime(toEpochMilli(time.atDate(LocalDate.now())))

  /**
   * @return 一天的开始：
   */
  def nowBegin(): LocalDateTime = LocalDate.now().atTime(0, 0, 0, 0)

  /**
   * @return 一天的结尾：
   */
  def nowEnd(): LocalDateTime =
    LocalTime.of(23, 59, 59, 999999999).atDate(LocalDate.now())

  def toDayInt(localDateTime: LocalDateTime): Int =
    toDayInt(localDateTime.toLocalDate)

  /**
   * 将 LocalDate(2017-11-21) 转换成 20171121 (Int类型)
   * @param localDate
   * @return
   */
  def toDayInt(localDate: LocalDate): Int =
    localDate.getYear * 10000 + localDate.getMonthValue * 100 + localDate.getDayOfMonth

  private[this] val funcId = new java.util.concurrent.atomic.AtomicInteger()

  def time[R](func: => R): R = {
    val fid = funcId.incrementAndGet()
    val start = Instant.now()
    logger.info(s"funcId: $fid start time: $start")
    try {
      func
    } finally {
      val end = Instant.now()
      val cost = Duration.between(start, end)
      logger.info(s"funcId: $fid end time: $end, cost: $cost")
    }
  }
}
