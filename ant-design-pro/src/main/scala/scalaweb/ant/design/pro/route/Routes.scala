package scalaweb.ant.design.pro.route

import akka.http.scaladsl.server.Route
import helloscala.http.route.AbstractRoute
import scalaweb.ant.design.pro.mock.Mocks

class Routes extends AbstractRoute {

  // #routes
  def route: Route =
    pathPrefix("api") {
      pathGet("currentUser") {
        complete(Mocks.apiCurrentUser)
      } ~
      pathGet("fake_chart_data") {
        complete(Mocks.apiFakeChartData)
      } ~
      pathGet("tags") {
        complete(Mocks.apiTags)
      } ~
      pathGet("activities") {
        complete(Mocks.apiActivities)
      } ~
      pathGet("fake_list") {
        parameter('count.as[Int]) { count =>
          complete(Mocks.apiFakeList(count))
        }
      } ~
      pathPrefix("project") {
        pathGet("notice") {
          complete(Mocks.project.notice)
        }
      }
    } ~
    notPathPrefixTest("api") {
      getFromResourceDirectory("dist") ~
      getFromResource("dist/index.html")
    }
  // #routes

}
