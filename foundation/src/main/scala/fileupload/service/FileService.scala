package fileupload.service

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Multipart
import akka.stream.Materializer
import fileupload.model.FileBO
import fileupload.model.FileMeta

import scala.concurrent.Future

trait FileService {

  implicit val system: ActorSystem
  implicit val mat: Materializer

  def progressByHash(hash: String): Future[Option[FileMeta]]

  def handleUpload(formData: Multipart.FormData): Future[Seq[FileBO]]

}

object FileService {

  def apply(system: ActorSystem, mat: Materializer) = new FileServiceImpl(system, mat)
}
