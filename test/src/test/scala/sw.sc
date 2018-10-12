import java.time.OffsetDateTime

import scala.math.Numeric.{IntIsIntegral, LongIsIntegral}

val str = "2018-10-09T02:03:38.685+08:00"

OffsetDateTime.parse(str)

IntIsIntegral.zero - IntIsIntegral.one
LongIsIntegral.zero - LongIsIntegral.one

