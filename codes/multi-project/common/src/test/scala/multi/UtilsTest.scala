package multi

import org.scalatest.{FunSuite, Matchers}

class UtilsTest extends FunSuite with Matchers {

  test("isBlank") {
    Utils.isBlank("   ") shouldBe true
    Utils.isBlank("") shouldBe true
    Utils.isBlank(null) shouldBe true
    Utils.isBlank("df") shouldBe false
    Utils.isBlank("df  ") shouldBe false
    Utils.isBlank("  df  ") shouldBe false
  }

}