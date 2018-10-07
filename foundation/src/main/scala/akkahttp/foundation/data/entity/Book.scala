package akkahttp.foundation.data.entity

case class Book(isbn: String, author: Long, title: String, amount: BigDecimal, description: Option[String]) {}
