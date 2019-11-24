package helloscala.common.util

import java.security.SecureRandom

import scala.annotation.tailrec
import scala.collection.immutable

object StringUtils {
  val BLACK_CHAR: Char = ' '
  val PRINTER_CHARS
      : immutable.IndexedSeq[Char] = ('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')
  private val HEX_CHARS: Array[Char] = "0123456789abcdef".toCharArray
  private val HEX_CHAR_SETS = Set
      .empty[Char] ++ ('0' to '9') ++ ('a' to 'f') ++ ('A' to 'F')

  def option(text: String): Option[String] =
    if (isBlank(text)) None else Some(text)

  @inline def isHex(c: Char): Boolean = HEX_CHAR_SETS.contains(c)

  /** Turns an array of Byte into a String representation in hexadecimal. */
  def hex2Str(bytes: Array[Byte]): String = {
    val hex = new Array[Char](2 * bytes.length)
    var i = 0
    while (i < bytes.length) {
      hex(2 * i) = HEX_CHARS((bytes(i) & 0xF0) >>> 4)
      hex(2 * i + 1) = HEX_CHARS(bytes(i) & 0x0F)
      i = i + 1
    }
    new String(hex)
  }

  /** Turns a hexadecimal String into an array of Byte. */
  def str2Hex(str: String): Array[Byte] = {
    val bytes = new Array[Byte](str.length / 2)
    var i = 0
    while (i < bytes.length) {
      bytes(i) = Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16).toByte
      i += 1
    }
    bytes
  }

  def isEmpty(s: CharSequence): Boolean = (s eq null) || s.length() == 0
  @inline def isNoneEmpty(s: CharSequence): Boolean = !isEmpty(s)

  def isBlank(s: CharSequence): Boolean = {
    @tailrec def isNoneBlankChar(s: CharSequence, i: Int): Boolean =
      if (i < s.length()) s.charAt(i) != BLACK_CHAR || isNoneBlankChar(s, i + 1)
      else false

    isEmpty(s) || !isNoneBlankChar(s, 0)
  }

  @inline
  def isNoneBlank(s: CharSequence): Boolean = !isBlank(s)

  def randomString(size: Int): String = {
    new SecureRandom()
    val random = SecureRandom.getInstanceStrong
    val len = PRINTER_CHARS.length
    (0 until size).map(_ => PRINTER_CHARS(random.nextInt(len))).mkString
  }

  /**
   * 字符串从属性形式转换为下划线形式
   *
   * @param name    待转字符串
   * @param isLower 转换成下划线形式后是否使用小写，false将完全使用大写
   * @return 转换后字符串
   */
  def convertPropertyToUnderscore(name: String, isLower: Boolean = true): String =
    if (isBlank(name)) name
    else {
      val sb = new StringBuilder
      for (c <- name) {
        if (Character.isUpperCase(c)) {
          sb.append('_')
        }
        sb.append(
          if (isLower) Character.toLowerCase(c)
          else Character.toUpperCase(c.toUpper))
      }
      sb.toString()
    }

  /**
   * Convert a column name with underscores to the corresponding property name using "camel case".  A name
   * like "customer_number" would match a "customerNumber" property name.
   *
   * @param name the column name to be converted
   * @return the name using "camel case"
   */
  def convertUnderscoreToProperty(name: String): String =
    if (isBlank(name)) ""
    else {
      val arr = name.split('_')
      arr.head + arr.tail.map(item => item.head.toUpper + item.tail).mkString
    }
}
