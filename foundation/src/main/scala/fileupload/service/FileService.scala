package fileupload.service

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Multipart
import akka.stream.ActorMaterializer
import fileupload.model.{FileBO, FileMeta}

import scala.concurrent.Future

trait FileService {

  implicit val system: ActorSystem
  implicit val mat: ActorMaterializer

  def progressByHash(hash: String): Future[Option[FileMeta]]

  def handleUpload(formData: Multipart.FormData): Future[Seq[FileBO]]

}

object FileService {

  def apply(system: ActorSystem, mat: ActorMaterializer) = new FileServiceImpl(system, mat)
}
