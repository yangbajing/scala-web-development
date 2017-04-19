package akkahttp.foundation.data.repository

import akkahttp.MeSpec
import akkahttp.foundation.data.entity.Author
import akkahttp.jdbc.JDBCSpec

/**
  * Created by yangbajing(yangbajing@gmail.com) on 2017-04-19.
  */
class AuthorRepositoryTest extends MeSpec with JDBCSpec {

  "AuthorRepositoryTest" should {
    val accountRepository = new AuthorRepository(dataSource)

    "create" in {
      val account = Author(0, "羊八井", Some(31), None)
      val result = accountRepository.create(account)
      result.id must be > 0L
    }

    "update" in {
      val author = Author(3, "yangbajing", Some(32), Some("中国重庆江津"))
      val result = accountRepository.update(author)
      result.id mustBe author.id
    }

    "list" in {
      val results = accountRepository.list()
      results must not be empty
      println(s"results size: ${results.size}")
    }

  }

}
