package me.yangbajing.util

import java.nio.ByteBuffer
import java.security.SecureRandom

import scala.util.matching.Regex

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-24.
 */
object Utils {
  val REGEX_DIGIT: Regex = """[\d,]+""".r
  val RANDOM_CHARS: IndexedSeq[Char] = ('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')

  val random: SecureRandom = SecureRandom.getInstanceStrong

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
      sb.append(RANDOM_CHARS.apply(random.nextInt(len)))
    }
    sb.toString()
  }
}
