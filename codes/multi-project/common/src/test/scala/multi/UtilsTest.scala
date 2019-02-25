package multi

import org.scalatest.{FunSuite, MustMatchers}

class UtilsTest extends FunSuite with MustMatchers {

  test("isBlank") {
    Utils.isBlank("   ") mustBe true
    Utils.isBlank("") mustBe true
    Utils.isBlank(null) mustBe true
    Utils.isBlank("df") mustBe false
    Utils.isBlank("df  ") mustBe false
    Utils.isBlank("  df  ") mustBe false
  }

}