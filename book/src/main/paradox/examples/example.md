
```scala

  val HEADER_KEYS = Set("api-id", "user-id", "org-id")

  def generateHeaders: Directive1[Map[String, String]] =
    extractRequest.flatMap { request =>
      val headerMap = request.headers
        .filter(header => HEADER_KEYS.contains(header.lowercaseName()))
        .map(header => header.lowercaseName() -> header.value())
        .toMap
      if (true) provide(headerMap)
      else reject(ForbiddenRejection("用户认证失败"))
    }

  def testRoute = generateHeaders { reqHeaders =>
    messageRoute(reqHeaders)
  }

  def messageRoute(reqHeaders: Map[String, String]): Route = {
    completeOk
  }

```