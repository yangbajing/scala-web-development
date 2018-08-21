package helloscala.common.util

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.MessageDigest

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink}
import org.bouncycastle.util.encoders.Hex

import scala.concurrent.Future

object MessageDigestAlgorithms {

  /**
   * The MD5 message digest algorithm defined in RFC 1321.
   */
  val MD5 = "MD5"

  /**
   * The SHA-1 hash algorithm defined in the FIPS PUB 180-2.
   */
  val SHA_1 = "SHA-1"

  /**
   * The SHA-256 hash algorithm defined in the FIPS PUB 180-2.
   */
  val SHA_256 = "SHA-256"

  /**
   * The SHA-512 hash algorithm defined in the FIPS PUB 180-2.
   */
  val SHA_512 = "SHA-512"
}

object DigestUtils {

  def digestMD5(): MessageDigest =
    MessageDigest.getInstance(MessageDigestAlgorithms.MD5)

  def digestSha1(): MessageDigest = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_1)

  def digestSha256(): MessageDigest = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_256)

  def digestSha512(): MessageDigest = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_512)

  def md5(data: Array[Byte]): Array[Byte] = {
    val digest = digestMD5()
    digest.update(data)
    digest.digest()
  }

  def md5Hex(data: Array[Byte]): String =
    Hex.toHexString(md5(data))

  def md5Hex(data: String): String =
    md5Hex(data.getBytes(StandardCharsets.UTF_8))

  def sha1(data: Array[Byte]): Array[Byte] = {
    val digest = digestSha1()
    digest.update(data)
    digest.digest()
  }

  def sha1Hex(data: Array[Byte]): String =
    Hex.toHexString(sha1(data))

  def sha1Hex(data: String): String =
    sha1Hex(data.getBytes(StandardCharsets.UTF_8))

  def sha256(data: Array[Byte]): Array[Byte] = {
    val digest = digestSha256()
    digest.update(data)
    digest.digest()
  }

  def sha256Hex(data: Array[Byte]): String =
    Hex.toHexString(sha256(data))

  def sha256Hex(data: String): String =
    sha256Hex(data.getBytes(StandardCharsets.UTF_8))

  def sha512(data: Array[Byte]): Array[Byte] = {
    val digest = digestSha512()
    digest.update(data)
    digest.digest()
  }

  def sha512Hex(data: Array[Byte]): String =
    Hex.toHexString(sha512(data))

  def sha512Hex(data: String): String =
    sha512Hex(data.getBytes(StandardCharsets.UTF_8))

  def reactiveSha256Hex(path: Path)(implicit mat: ActorMaterializer): Future[String] = {
    import mat.executionContext
    reactiveSha256(path).map(bytes => Hex.toHexString(bytes))
  }

  def reactiveSha256(path: Path)(implicit mat: ActorMaterializer): Future[Array[Byte]] = {
    import mat.executionContext
    val md = digestSha256()
    FileIO
      .fromPath(path)
      .map(bytes => md.update(bytes.asByteBuffer))
      .runWith(Sink.ignore)
      .map(_ => md.digest())
  }
}
