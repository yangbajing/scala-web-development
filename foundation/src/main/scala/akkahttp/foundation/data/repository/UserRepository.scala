package akkahttp.foundation.data.repository

import java.nio.ByteBuffer
import java.util.UUID

import akka.Done
import akka.stream.Materializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.Sink
import akkahttp.foundation.data.entity.User
import com.datastax.driver.core.Row
import me.yangbajing.cassandra.StandaloneCassandraSession
import me.yangbajing.util.SaltPassword
import me.yangbajing.util.TimeUtils
import me.yangbajing.util.Utils

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-24.
 */
class UserRepository(cassandraSession: StandaloneCassandraSession)(
    implicit val materializer: Materializer,
    ec: ExecutionContext) {

  import cassandraSession.session

  def deleteById(userId: UUID): Future[Done] = {
    val stmt = cassandraSession.prepare("delete from hldev.user where id = ?").bind(userId)
    CassandraSource(stmt).runWith(Sink.ignore)
  }

  def insert(user: User, password: SaltPassword): Future[Done] = {
    val stmt = cassandraSession
      .prepare("insert into hldev.user(id, email, name, created_at, salt, salt_password) values(?, ?, ?, ?, ?, ?)")
      .bind(
        user.id,
        user.email,
        user.email.split('@')(0),
        user.createdAt,
        ByteBuffer.wrap(password.salt),
        ByteBuffer.wrap(password.saltPwd))
    CassandraSource(stmt).runWith(Sink.ignore)
  }

  /**
   * 用户登录
   *
   * @param email    登录邮箱账号
   * @param password 登录密码
   * @return Option(User, Salt Bytes, Salt Password Bytes)
   */
  def login(email: String, password: String): Future[Option[(User, Array[Byte], Array[Byte])]] = {
    val stmt = cassandraSession.prepare("select * from hldev.user where email = ? ALLOW FILTERING").bind(email)
    CassandraSource(stmt)
      .runWith(Sink.headOption) // (3)
      .map(
        maybeRow => // (4)
          maybeRow.map(
            row =>
              (
                UserRepository.mapToUser(row),
                Utils.byteBufferToArray(row.getBytes("salt")),
                Utils.byteBufferToArray(row.getBytes("salt_password")))))
  }

  def findById(userId: UUID): Future[Option[User]] = {
    val stmt = cassandraSession.prepare("select * from hldev.user where id = ?").bind(userId)
    CassandraSource(stmt).runWith(Sink.headOption).map(maybeRow => maybeRow.map(row => UserRepository.mapToUser(row)))
  }

  def existsByEmail(email: String): Future[Boolean] =
    CassandraSource(
      cassandraSession.prepare("select count(1) as COUNT from hldev.user where email = ? ALLOW FILTERING").bind(email))
      .runWith(Sink.head)
      .map(row => row.getLong("COUNT") == 1)
}

object UserRepository {

  private def mapToUser(row: Row) =
    User(
      row.getUUID("id"),
      row.getString("email"),
      row.getString("name"),
      TimeUtils.toLocalDateTime(row.getTimestamp("created_at")))

}
