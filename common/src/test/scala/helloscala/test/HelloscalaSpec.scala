package helloscala.test

import org.scalatest.EitherValues
import org.scalatest.Matchers
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.WordSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Milliseconds
import org.scalatest.time.Seconds
import org.scalatest.time.Span

trait FusionTestWordSpec extends WordSpecLike with OptionValues with EitherValues

trait HelloscalaSpec extends FusionTestWordSpec with ScalaFutures with Matchers {
  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(Span(60, Seconds), Span(200, Milliseconds))
}
