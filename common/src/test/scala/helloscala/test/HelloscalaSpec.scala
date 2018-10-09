package helloscala.test

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{EitherValues, MustMatchers, OptionValues, WordSpecLike}

trait HelloscalaSpec extends WordSpecLike with MustMatchers with OptionValues with EitherValues with ScalaFutures {
  implicit override val patienceConfig: PatienceConfig = PatienceConfig(Span(60, Seconds), Span(200, Milliseconds))
}
