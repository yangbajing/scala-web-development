package akkahttp.foundation.data.repository

import akkahttp.foundation.data.entity.Author
import me.yangbajing.MeSpec
import me.yangbajing.jdbc.JDBCSpec

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-19.
 */
// #AuthorRepositoryTest
class AuthorRepositoryTest extends MeSpec with JDBCSpec {
  "AuthorRepositoryTest" should {
    val accountRepository = new AuthorRepository(dataSource)

    "create" in {
      val account = Author(0, "羊八井", Some(31), None)
      val result = accountRepository.create(account)
      result.id should be > 0L
    }

    "update" in {
      val author = Author(1, "羊八井", Some(32), None)
      val result = accountRepository.update(author)
      result.id shouldBe author.id
      result.name shouldBe author.name
      result.age shouldBe author.age
    }

    "list" in {
      val results = accountRepository.list()
      results should not be empty
      println(s"results size: ${results.size}")
    }
  }
}
// #AuthorRepositoryTest
