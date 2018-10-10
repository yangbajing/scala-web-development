package book.example.route

import akka.http.scaladsl.server.{Directive, Directive0, Directive1, Route}
import helloscala.http.route.AbstractRoute

case class User(id: Option[Int], username: String, age: Int)

class RouteExample extends AbstractRoute {

  import helloscala.http.JacksonSupport._

  // #basic-route-tree
  val route: Route =
    pathPrefix("user") {
      pathEndOrSingleSlash { // POST /user
        post {
          entity(as[User]) { payload =>
            complete(payload)
          }
        }
      } ~
        pathPrefix(IntNumber) { userId =>
          get { // GET /user/{userId}
            complete(User(Some(userId), "", 0))
          } ~
            put { // PUT /user/{userId}
              entity(as[User]) { payload =>
                complete(payload)
              }
            } ~
            delete { // DELETE /user/{userId}
              complete("Deleted")
            }
        }
    }
  // #basic-route-tree

  // #basic-route-1
  val route1: Route =
    pathPrefix("user") {
      pathEndOrSingleSlash {
        post {
          entity(as[User]) { payload =>
            complete(payload)
          }
        }
      } ~
        pathPrefix(IntNumber) { userId =>
          innerUser(userId)
        }
    }

  def innerUser(userId: Int): Route =
    get {
      complete(User(Some(userId), "", 0))
    } ~
      put {
        entity(as[User]) { payload =>
          complete(payload)
        }
      } ~
      delete {
        complete("Deleted")
      }
  // #basic-route-1

  // #directive-and
  val pathEndPost: Directive[Unit] = pathEndOrSingleSlash & post

  val createUser: Route = pathEndPost {
    entity(as[User]) { payload =>
      complete(payload)
    }
  }
  // #directive-and

  // #directive-or
  val deleteEnhance: Directive1[Int] =
    (pathPrefix(IntNumber) & delete) | (path(IntNumber / "_delete") & put)

  val deleteUser: Route = deleteEnhance { userId =>
    complete(s"Deleted User, userId: $userId")
  }
  // #directive-or

  // #deleteUser2
  val deleteUser2 = pathPrefix(IntNumber) { userId =>
    overrideMethodWithParameter("httpMethod") {
      delete {
        complete(s"Deleted User, userId: $userId")
      }
    }
  }
  // #deleteUser2


}
