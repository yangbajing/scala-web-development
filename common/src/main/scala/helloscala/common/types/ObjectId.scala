package helloscala.common.types

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import com.fasterxml.jackson.annotation.JsonIgnore
import helloscala.common.util.DigestUtils
import helloscala.common.util.StringUtils

import scala.util.Failure
import scala.util.Try

/**
 * BSON ObjectId value.
 *
 * +------------------------+------------------------+------------------------+------------------------+
 * + timestamp (in seconds) +   machine identifier   +    thread identifier   +        increment       +
 * +        (4 bytes)       +        (3 bytes)       +        (2 bytes)       +        (3 bytes)       +
 * +------------------------+------------------------+------------------------+------------------------+
 */
@SerialVersionUID(239421902L) //@ApiModel(parent = classOf[String])
class ObjectId private (private val raw: Array[Byte])
    extends Serializable
    with Equals {
  /** ObjectId hexadecimal String representation */
  @JsonIgnore
  lazy val stringify: String = StringUtils.hex2Str(raw)

  override def toString() = stringify

  override def canEqual(that: Any): Boolean = that.isInstanceOf[ObjectId]

  override def equals(that: Any): Boolean = that match {
    case other: ObjectId => java.util.Arrays.equals(raw, other.raw)
    case _               => false
  }

  @JsonIgnore
  override lazy val hashCode: Int = java.util.Arrays.hashCode(raw)

  /** The time of this BSONObjectId, in milliseconds */
  def time: Long = this.timeSecond * 1000L

  /** The time of this BSONObjectId, in seconds */
  def timeSecond: Int = ByteBuffer.wrap(raw.take(4)).getInt

  def valueAsArray: Array[Byte] = java.util.Arrays.copyOf(raw, 12)
}

object ObjectId {
  val STR_LENGTH = 24
  private val maxCounterValue = 16777216
  private val increment =
    new java.util.concurrent.atomic.AtomicInteger(
      scala.util.Random.nextInt(maxCounterValue))

  private def counter() =
    (increment.getAndIncrement + maxCounterValue) % maxCounterValue

  /**
   * The following implemtation of machineId work around openjdk limitations in
   * version 6 and 7
   *
   * Openjdk fails to parse /proc/net/if_inet6 correctly to determine macaddress
   * resulting in SocketException thrown.
   *
   * Please see:
   * * https://github.com/openjdk-mirror/jdk7u-jdk/blob/feeaec0647609a1e6266f902de426f1201f77c55/src/solaris/native/java/net/NetworkInterface.c#L1130
   * * http://lxr.free-electrons.com/source/net/ipv6/addrconf.c?v=3.11#L3442
   * * http://lxr.free-electrons.com/source/include/linux/netdevice.h?v=3.11#L1130
   * * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7078386
   *
   * and fix in openjdk8:
   * * http://hg.openjdk.java.net/jdk8/tl/jdk/rev/b1814b3ea6d3
   */
  private val machineId = {
    import java.net._
    def p(n: String) = System.getProperty(n)

    val validPlatform = Try {
      val correctVersion = p("java.version").substring(0, 3).toFloat >= 1.8
      val noIpv6 = p("java.net.preferIPv4Stack").toBoolean
      val isLinux = p("os.name") == "Linux"

      !isLinux || correctVersion || noIpv6
    }.getOrElse(false)

    // Check java policies
    val permitted =
      Try(
        System.getSecurityManager.checkPermission(
          new NetPermission("getNetworkInformation"))).toOption.exists(_ => true)

    if (validPlatform && permitted) {
      val networkInterfacesEnum = NetworkInterface.getNetworkInterfaces
      val networkInterfaces =
        scala.collection.JavaConverters
          .enumerationAsScalaIteratorConverter(networkInterfacesEnum)
          .asScala
      val ha = networkInterfaces
        .find(ha =>
          Try(ha.getHardwareAddress).isSuccess && ha.getHardwareAddress != null && ha.getHardwareAddress.length == 6)
        .map(_.getHardwareAddress)
        .getOrElse(
          InetAddress.getLocalHost.getHostName.getBytes(StandardCharsets.UTF_8))
      DigestUtils.md5(ha).take(3)
    } else {
      val threadId = Thread.currentThread.getId.toInt
      val arr = new Array[Byte](3)

      arr(0) = (threadId & 0xFF).toByte
      arr(1) = (threadId >> 8 & 0xFF).toByte
      arr(2) = (threadId >> 16 & 0xFF).toByte

      arr
    }
  }

  //  implicit def string2ObjectId(id: String): ObjectId = apply(id)

  // Java API
  def create(id: String): ObjectId = apply(id)

  // Java API
  def create(array: Array[Byte]): ObjectId = apply(array)

  /**
   * Constructs a BSON ObjectId element from a hexadecimal String representation.
   * Throws an exception if the given argument is not a valid ObjectID.
   */
  def apply(id: String): ObjectId = parse(id) match {
    case scala.util.Success(value) => value
    case scala.util.Failure(e)     => throw e
  }

  def apply(array: Array[Byte]): ObjectId = {
    if (array.length != 12)
      throw new IllegalArgumentException(
        s"wrong byte array for an ObjectId (size ${array.length})")
    new ObjectId(java.util.Arrays.copyOf(array, 12))
  }

  def unapply(id: ObjectId): Option[Array[Byte]] = Some(id.valueAsArray)

  /** Tries to make a BSON ObjectId from a hexadecimal string representation. */
  def parse(id: String): Try[ObjectId] =
    if (isValid(id)) Try(new ObjectId(StringUtils.str2Hex(id)))
    else
      Failure(
        new IllegalArgumentException(
          s"Wrong ObjectId (It is not a valid 16 Decimal 24 bit string): '$id'"))

  def isValid(id: String): Boolean =
    StringUtils.isNoneBlank(id) && id.length == 24 && id.forall(StringUtils.isHex)

  def isValid(ids: Iterable[String]): Boolean =
    ids.forall(isValid)

  @inline def validation(id: String, msgPrefix: String = ""): Unit =
    require(isValid(id), s"$msgPrefix，$id 格式无效")

  @inline def validation(ids: Iterable[String], msgPrefix: String): Unit =
    if (ids.nonEmpty) {
      ids.foreach(id => validation(id, msgPrefix))
    }

  /**
   * Generates a new BSON ObjectID using the current time.
   *
   * @see [[fromTime]]
   */
  def generate(): ObjectId = get()

  def get(): ObjectId =
    fromTime(System.currentTimeMillis, fillOnlyTimestamp = false)

  def getString(): String = get().toString()

  /**
   * Generates a new BSON ObjectID from the given timestamp in milliseconds.
   *
   * The included timestamp is the number of seconds since epoch, so a ObjectId time part has only
   * a precision up to the second. To get a reasonably unique ID, you _must_ set `onlyTimestamp` to false.
   *
   * Crafting a ObjectId from a timestamp with `fillOnlyTimestamp` set to true is helpful for range queries,
   * eg if you want of find documents an _id field which timestamp part is greater than or lesser than
   * the one of another id.
   *
   * If you do not intend to use the produced ObjectId for range queries, then you'd rather use
   * the `generate` method instead.
   *
   * @param fillOnlyTimestamp if true, the returned ObjectId will only have the timestamp bytes set; the other will be set to zero.
   */
  def fromTime(timeMillis: Long, fillOnlyTimestamp: Boolean = true): ObjectId = {
    // n of seconds since epoch. Big endian
    val timestamp = (timeMillis / 1000).toInt
    val id = new Array[Byte](12)

    id(0) = (timestamp >>> 24).toByte
    id(1) = (timestamp >> 16 & 0xFF).toByte
    id(2) = (timestamp >> 8 & 0xFF).toByte
    id(3) = (timestamp & 0xFF).toByte

    if (!fillOnlyTimestamp) {
      // machine id, 3 first bytes of md5(macadress or hostname)
      id(4) = machineId(0)
      id(5) = machineId(1)
      id(6) = machineId(2)

      // 2 bytes of the pid or thread id. Thread id in our case. Low endian
      val threadId = Thread.currentThread.getId.toInt
      id(7) = (threadId & 0xFF).toByte
      id(8) = (threadId >> 8 & 0xFF).toByte

      // 3 bytes of counter sequence, which start is randomized. Big endian
      val c = counter()
      id(9) = (c >> 16 & 0xFF).toByte
      id(10) = (c >> 8 & 0xFF).toByte
      id(11) = (c & 0xFF).toByte
    }

    ObjectId(id)
  }
}
