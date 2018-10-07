package helloscala.common.util

import java.nio.ByteBuffer
import java.nio.file.{Files, Path}
import java.security.SecureRandom
import java.time.{LocalDate, LocalDateTime}
import java.util.concurrent.ThreadLocalRandom
import java.util.{Properties, Random}

import scala.util.Try
import scala.util.matching.Regex

object Utils {

  val REGEX_DIGIT: Regex = """[\d,]+""".r
  val RANDOM_CHARS: IndexedSeq[Char] = ('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')

  val random: SecureRandom = new SecureRandom()

  def swap[X, Y](x: X, y: Y): (Y, X) = (y, x)

  /**
   * 将数组原地乱序
   * @param arr 原打乱元素顺序的数组
   * @return 已打乱顺序的数组
   */
  def random[@specialized(Specializable.AllNumeric) T](arr: Array[T]): Array[T] = {
    val random = new Random()
    var i = 0
    val len = arr.length
    while (i < len) {
      val si = random.nextInt(len)
      if (i != si) {
        val tmp = arr(si)
        arr(si) = arr(i)
        arr(i) = tmp
        i += 2
      } else {
        i += 1
      }
    }
    arr
  }

  /**
   * 将字符串解析为数字
   *
   * @param s 字符串
   * @return
   */
  def parseInt(s: CharSequence): Option[Int] =
    REGEX_DIGIT.findFirstIn(s).map(_.replaceAll(",", "").toInt)

  def parseInt(s: CharSequence, deft: => Int): Int =
    parseInt(s).getOrElse(deft)

  def parseInt(a: Any, deft: => Int): Int =
    parseInt(a.toString, deft)

  def parseIntAll(s: CharSequence): List[Int] = {
    val iter = REGEX_DIGIT.findAllIn(s)
    var list = List.empty[Int]
    while (iter.hasNext) {
      list = iter.next().toInt :: list
    }
    list
  }

  def parseLong(s: Any, deft: => Long): Long = parseLong(s).getOrElse(deft)

  def parseLong(s: Any): Option[Long] =
    s match {
      case l: Long    => Some(l)
      case i: Int     => Some(i.toLong)
      case s: String  => Try(s.toLong).toOption
      case bi: BigInt => Some(bi.longValue())
      case _          => None
    }

  def isNoneBlank(content: String): Boolean = !isBlank(content)

  def isBlank(content: String): Boolean =
    content == null || content.isEmpty || content.forall(Character.isWhitespace)

  def byteBufferToArray(buf: ByteBuffer): Array[Byte] = {
    val dst = new Array[Byte](buf.remaining())
    buf.get(dst)
    dst
  }

  def randomString(n: Int): String = {
    val len = RANDOM_CHARS.length
    val sb = new StringBuilder
    var i = 0
    while (i < n) {
      i += 1
      val idx = ThreadLocalRandom.current().nextInt(len)
      val c = RANDOM_CHARS.apply(idx)
      sb.append(c)
    }
    sb.toString()
  }

  /**
   * 从目录读取所有文件的所有行，并过滤掉空行（包括空白字符行）
   *
   * @param dir 目录
   * @return
   */
  def readAllLinesFromPath(dir: Path): java.util.stream.Stream[String] = {
    val filterNoneBlank: String => Boolean = s => StringUtils.isNoneBlank(s)
    val trim: String => String = s => s.trim
//    val trans: Path => java.util.stream.Stream[String] = path => Files.readAllLines(path).stream()
    Files
      .list(dir)
      .flatMap[String](path => Files.readAllLines(path).stream())
      .map[String](s => s.trim)
      .filter(s => StringUtils.isNoneBlank(s))
  }

  def randomBytes(size: Int): Array[Byte] = {
    val buf = new Array[Byte](size)
    random.nextBytes(buf)
    buf
  }

  def parseSeq(str: String, splitChar: Char = ','): Vector[String] =
    str.split(splitChar).filter(s => StringUtils.isNoneBlank(s)).toVector

  def mapToJMap[K, V](map: Map[K, V]): java.util.Map[K, V] = {
    val m = new java.util.HashMap[K, V]()
    map.foreach { case (k, v) => m.put(k, v) }
    m
  }

  def boxed(v: Any): Object = v match {
    case i: Int      => Int.box(i)
    case l: Long     => Long.box(l)
    case d: Double   => Double.box(d)
    case s: Short    => Short.box(s)
    case f: Float    => Float.box(f)
    case c: Char     => Float.box(c)
    case b: Boolean  => Boolean.box(b)
    case b: Byte     => Byte.box(b)
    case obj: AnyRef => obj
    case o           => o.asInstanceOf[Object]
  }

  def sqlBoxed(v: Any): Object = v match {
    case ldt: LocalDateTime => TimeUtils.toSqlTimestamp(ldt)
    case ld: LocalDate      => TimeUtils.toSqlDate(ld)
    case o                  => o.asInstanceOf[Object]
  }

  def boxedSQL(v: Any): Object =
    try {
      boxed(v)
    } catch {
      case _: Throwable =>
        sqlBoxed(v)
    }

  def isEmail(account: String): Boolean =
    // TODO
    account.contains('@')

  @inline
  def option(s: String): Option[String] = Some(s).filter(str => StringUtils.isNoneBlank(str))

  @inline
  def option[V](v: V): Option[V] = Option(v)

  def propertiesToMap(props: Properties): Map[String, String] = {
    import scala.collection.JavaConverters._
    props
      .stringPropertyNames()
      .asScala
      .map(name => name -> props.getProperty(name))
      .toMap
  }

}
