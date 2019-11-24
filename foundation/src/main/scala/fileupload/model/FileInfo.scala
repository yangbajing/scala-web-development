package fileupload.model

import java.nio.file.Path

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.Multipart.FormData
import fileupload.Constants

import scala.collection.immutable

case class FileInfo(
    bodyPart: FormData.BodyPart,
    hash: Option[String],
    contentLength: Long,
    startPosition: Long) {
  override def toString: String =
    s"FileInfo(${bodyPart.name}, $hash, $contentLength, $startPosition, ${bodyPart.filename}, ${bodyPart.headers})"
}

object FileInfo {
  val Empty = FileInfo(null, None, 0L, 0L)

  def apply(part: FormData.BodyPart): FileInfo = {
    val (hash, contentLength, startPosition) = part.name.split('.') match {
      case Array(a, b, c) => (a, b.toLong, c.toLong)
      case Array(a, b)    => (a, b.toLong, 0L)
      case Array(a)       => (a, 0L, 0L)
      case _ =>
        throw new IllegalArgumentException(
          s"Multipart.FormData name格式不符合要求：${part.name}")
    }
    new FileInfo(
      part,
      if (Constants.HASH_LENGTH == hash.length) Some(hash) else None,
      contentLength,
      startPosition)
  }
}

/**
 * 文件元数据
 * @param hash 文件HASH（sha256）
 * @param size 已上传（bytes）
 * @param localPath 本地存储路径
 */
case class FileMeta(hash: String, size: Long, localPath: Path)

case class FileBO(
    hash: Option[String],
    computedHash: Option[String],
    localPath: Path,
    contentLength: Long,
    filename: Option[String],
    headers: immutable.Seq[HttpHeader])
