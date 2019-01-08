package fileupload.util
import akka.http.scaladsl.coding.Encoder

object Utils {
  private val hexArray = "0123456789abcdef".toCharArray

  def bytesToHex(bytes: Array[Byte]): String = {
    val hexChars = new Array[Char](bytes.length * 2)
    var j = 0
    while (j < bytes.length) {
      val v = bytes(j) & 0xFF
      hexChars(j * 2) = hexArray(v >>> 4)
      hexChars(j * 2 + 1) = hexArray(v & 0x0F)
      j += 1
    }
    new String(hexChars)
  }

  Encoder
}
