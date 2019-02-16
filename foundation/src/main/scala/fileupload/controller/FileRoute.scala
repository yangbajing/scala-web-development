package fileupload.controller

import akka.http.scaladsl.model.{Multipart, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import com.typesafe.scalalogging.StrictLogging
import fileupload.service.FileService
import fileupload.util.FileUtils

class FileRoute(fileService: FileService) extends StrictLogging {

  def route: Route = pathPrefix("file") {
    log {
      uploadRoute ~
        downloadRoute ~
        progressRoute
    }
  }

  // #uploadRoute
  private def uploadRoute: Route = path("upload") {
    post {
      withoutSizeLimit {
        entity(as[Multipart.FormData]) { formData =>
          onSuccess(fileService.handleUpload(formData)) { results =>
            import helloscala.http.JacksonSupport._
            complete(results)
          }
        }
      }
    }
  }
  // #uploadRoute

  // #downloadRoute
  // 支持断点续传
  private def downloadRoute: Route = path("download" / Segment) { hash =>
    getFromFile(FileUtils.getLocalPath(hash).toFile)
  }
  // #downloadRoute

  // #progressRoute
  // 查询文件上传进度
  private def progressRoute: Route = path("progress" / Segment) { hash =>
    onSuccess(fileService.progressByHash(hash)) {
      case Some(v) =>
        import helloscala.http.JacksonSupport._
        complete(v)
      case None => complete(StatusCodes.NotFound)
    }
  }
  // #progressRoute

  private val log: Directive0 = extractRequest.flatMap { req =>
    logger.debug(req.toString())
    pass
  }

}
