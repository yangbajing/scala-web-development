import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val f = Future { 34 }

f.foreach()
