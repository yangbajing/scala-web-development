package akkahttp.foundation.data.entity

import java.time.LocalDateTime
import java.util.UUID

/**
  * Created by yangbajing(yangbajing@gmail.com) on 2017-04-24.
  */
case class User(id: UUID,
                email: String = "",
                name: String = "",
                createdAt: LocalDateTime = LocalDateTime.now())
