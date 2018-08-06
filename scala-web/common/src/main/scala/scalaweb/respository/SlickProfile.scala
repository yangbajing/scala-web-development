package scalaweb.respository

import com.github.tminglei.slickpg.{ExPostgresProfile, PgArraySupport, PgDate2Support}

trait SlickProfile extends ExPostgresProfile with PgDate2Support with PgArraySupport {
  override val api: MyAPI.type = MyAPI
  object MyAPI extends super.API with DateTimeImplicits with ArrayImplicits {}
}

object SlickProfile extends SlickProfile
