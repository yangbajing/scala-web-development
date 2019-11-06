package fileupload.util

import java.nio.file.StandardOpenOption.APPEND
import java.nio.file._
import java.security.MessageDigest

import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import com.typesafe.scalalogging.StrictLogging
import fileupload.Constants
import fileupload.model.FileBO
import fileupload.model.FileInfo
import fileupload.model.FileMeta

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object FileUtils extends StrictLogging {
  val TMP_DIR: Path = getOrCreateDirectories(Paths.get("/tmp/file-upload/tmp"))

  val LOCAL_PATH: String = getOrCreateDirectories(Paths.get("/tmp/file-upload")).toString

  def getOrCreateDirectories(path: Path): Path = {
    if (!Files.isDirectory(path)) {
      Files.createDirectories(path)
    }
    path
  }

  // #getLocalPath
  def getLocalPath(hash: String): Path = Paths.get(LOCAL_PATH, hash.take(2), hash)
  // #getLocalPath

  def getFileMeta(hash: String): Option[FileMeta] = {
    if (hash == null || Constants.HASH_LENGTH != hash.length) {
      None
    } else {
      val path = getLocalPath(hash)
      if (Files.exists(path) && Files.isReadable(path)) Some(FileMeta(hash, Files.size(path), path)) else None
    }
  }

  // #uploadFile
  def uploadFile(fileInfo: FileInfo)(implicit mat: Materializer, ec: ExecutionContext): Future[FileBO] = {
    // TODO 需要校验上传完成文件的hash值与提交hash值是否匹配？
    val maybeMeta = fileInfo.hash.flatMap(FileUtils.getFileMeta)
    val beContinue = maybeMeta.isDefined && fileInfo.startPosition > 0L
    val f = if (beContinue) uploadContinue(fileInfo, maybeMeta.get) else uploadNewFile(fileInfo)
    f.andThen {
      case tryValue =>
        logger.debug(s"文件上传完成：$tryValue")
    }
  }
  // #uploadFile

  // #uploadContinue
  private def uploadContinue(fileInfo: FileInfo, meta: FileMeta)(implicit mat: Materializer, ec: ExecutionContext) = {
    val bodyPart = fileInfo.bodyPart
    val localPath = FileUtils.getLocalPath(fileInfo.hash.get)
    logger.debug(s"断点续传，startPosition：${fileInfo.startPosition}，路径：$localPath")
    bodyPart.entity.dataBytes
      .runWith(FileIO.toPath(localPath, Set(APPEND), fileInfo.startPosition))
      .map(ioResult =>
        FileBO(fileInfo.hash, None, localPath, meta.size + ioResult.count, bodyPart.filename, bodyPart.headers))
  }
  // #uploadContinue

  // #uploadNewFile
  private def uploadNewFile(fileInfo: FileInfo)(implicit mat: Materializer, ec: ExecutionContext) = {
    val bodyPart = fileInfo.bodyPart
    val tmpPath = fileInfo.hash // (1)
      .map(h => FileUtils.getLocalPath(h))
      .getOrElse(Files.createTempFile(FileUtils.TMP_DIR, bodyPart.filename.getOrElse(""), ""))
    val sha = MessageDigest.getInstance("SHA-256")
    logger.debug(s"新文件，路径：$tmpPath")
    bodyPart.entity.dataBytes
      .map { byteString =>
        byteString.asByteBuffers.foreach(sha.update) // (2)
        byteString
      }
      .runWith(FileIO.toPath(tmpPath)) // (3)
      .map { ioResult =>
        val computedHash = Utils.bytesToHex(sha.digest()) // (4)
        fileInfo.hash.foreach { h =>
          require(h == computedHash, s"前端上传hash与服务端计算hash值不匹配，$h != $computedHash")
        }
        val localPath = fileInfo.hash match { // (5)
          case Some(_) => tmpPath
          case _       => move(computedHash, tmpPath, ioResult.count)
        }
        FileBO(fileInfo.hash, Some(computedHash), localPath, ioResult.count, bodyPart.filename, bodyPart.headers)
      }
  }
  // #uploadNewFile

  def move(hash: String, tmpFile: Path, contentLength: Long): Path = {
    val targetDir = FileUtils.getOrCreateDirectories(Paths.get(FileUtils.LOCAL_PATH, hash.take(2)))
    val target = targetDir.resolve(hash)
    require(!Files.exists(target), s"目标文件已存在，$target")
    Files.move(tmpFile, target)
  }

}
