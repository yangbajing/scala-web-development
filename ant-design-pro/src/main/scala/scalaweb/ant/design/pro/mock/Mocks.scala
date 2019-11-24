package scalaweb.ant.design.pro.mock

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import org.mozilla.javascript.Context

object Mocks {
  // #dashboard-mock-api
  object project {
    def notice = toJsonEntity(Project.notice)
  }
  def apiCurrentUser = toJsonEntity(Api.currentUser)
  def apiFakeChartData = toJsonEntity(Api.fake_chart_data)
  def apiTags = toJsonEntity(Api.tags)
  def apiActivities = toJsonEntity(Api.activities)

  def toJsonEntity(str: String): HttpEntity.Strict =
    HttpEntity(ContentTypes.`application/json`, str)
  // #dashboard-mock-api

  def apiFakeList(count: Int) = formatJsonString(s"fakeList($count)")

  def renderJsonEntity(str: String): HttpEntity.Strict =
    HttpEntity(ContentTypes.`application/json`, formatJsonString(str))

  private def createContext() = {
    val c = Context.enter()
    val scope = c.initStandardObjects()
    c.evaluateString(scope, Api.variables, "<Api.variables>", 1, null)
    c.evaluateString(
      scope,
      Api.functionFakeList,
      "<Api.functionFakeList>",
      1,
      null)
    c -> scope
  }

  private def formatJsonString(
      objStr: String,
      replacer: String = null,
      space: Int = 0): String = {
    val (cx, scope) = createContext()
    try {
      val content =
        s"""
         |var obj = $objStr;
         |JSON.stringify(obj, $replacer, $space);
      """.stripMargin
      //    scriptJavascript.eval(content).asInstanceOf[String]
      cx.evaluateString(scope, content, null, 1, null).asInstanceOf[String]
    } finally Context.exit()
  }

//  def main(args: Array[String]): Unit = {
//    val result = cx.evaluateString(scope, """fakeList(5)""", null, 1, null)
//    println(formatJsonString("fakeList(5)"))
//  }
}
