package me.yangbajing.jdbc

import java.sql.ResultSet
import java.time.LocalDateTime

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-19.
 */
class WrappedResultSet(val underlying: ResultSet) {
  def getBigDecimal(column: String): BigDecimal = {
    val value = underlying.getBigDecimal(column)
    if (value == null) null else BigDecimal(value)
  }

  def getBigDecimal(column: Int): BigDecimal = {
    val value = underlying.getBigDecimal(column)
    if (value == null) null else BigDecimal(value)
  }

  def getLocalDateTime(column: String): LocalDateTime = {
    val value = underlying.getTimestamp(column)
    if (value == null) null else value.toLocalDateTime
  }

  def getLocalDateTime(column: Int): LocalDateTime = {
    val value = underlying.getTimestamp(column)
    if (value == null) null else value.toLocalDateTime
  }
}
