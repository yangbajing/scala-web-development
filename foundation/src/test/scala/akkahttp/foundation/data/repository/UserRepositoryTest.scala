package akkahttp.foundation.data.repository

import akka.Done
import akkahttp.foundation.data.entity.User
import com.datastax.driver.core.utils.UUIDs
import me.yangbajing.MeSpec
import me.yangbajing.cassandra.CassandraSpec
import me.yangbajing.util.SecurityUtils

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-24.
 */
class UserRepositoryTest extends MeSpec with CassandraSpec {
  private var userRepository: UserRepository = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    userRepository = new UserRepository(cassandraSession)
  }

  "UserRepositoryTest" should {
    val userId = UUIDs.timeBased()

    "existsByEmail" in {
      userRepository.existsByEmail("yangbajing@gmail.com").futureValue shouldBe false
    }

    "insert" in {
      val user = User(userId, "yangbajing@gmail.com", "羊八井")
      userRepository.insert(user, SecurityUtils.generatePassword("yangbajing")).futureValue shouldBe Done
    }

    "login" in {
      val result = userRepository.login("yangbajing@gmail.com", "yangbajing").futureValue
      result should not be empty
      val (user, salt, saltPwd) = result.value
      user.name shouldBe "yangbajing"
      user.email shouldBe "yangbajing@gmail.com"
      SecurityUtils.matchSaltPassword(salt, saltPwd, "yangbajing".getBytes) shouldBe true
    }

    "findById" in {
      val result = userRepository.findById(userId).futureValue
      result should not be empty
      val user = result.value
      user.name shouldBe "yangbajing"
      user.email shouldBe "yangbajing@gmail.com"
    }

    "deleteById" in {
      userRepository.deleteById(userId).futureValue shouldBe Done
      userRepository.findById(userId).futureValue shouldBe empty
    }

  }

}
