package helloscala.test

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Milliseconds
import org.scalatest.time.Seconds
import org.scalatest.time.Span
import org.scalatest.EitherValues
import org.scalatest.MustMatchers
import org.scalatest.OptionValues
import org.scalatest.WordSpecLike

trait HelloscalaSpec extends WordSpecLike with MustMatchers with OptionValues with EitherValues with ScalaFutures {
  implicit override val patienceConfig: PatienceConfig = PatienceConfig(Span(60, Seconds), Span(200, Milliseconds))
}
