package io.github.ffossum.gamesitescala

import cats.effect.IO
import cats.implicits._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.github.ffossum.gamesitescala.JwtMiddleware.setJwtCookie
import io.github.ffossum.gamesitescala.db.Users._
import io.github.ffossum.gamesitescala.user.{Email, Password, Username}
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpService}

case class Registration(username: Username, email: Email, password: Password)
object Registration {
  implicit val registrationDecoder: Decoder[Registration]                 = deriveDecoder
  implicit val registrationEntityDecoder: EntityDecoder[IO, Registration] = jsonOf[IO, Registration]
}

class RegistrationService extends Http4sDsl[IO] {
  val service: HttpService[IO] = {
    HttpService[IO] {
      case req @ POST -> Root => {
        val res = for {
          registration <- req.as[Registration].attemptT
          user         <- createUser(registration.username, registration.email, registration.password)
          privateUserData = user.toPrivateUserData
        } yield Ok(privateUserData.asJson).map(setJwtCookie(privateUserData))

        res.value.flatMap(_.getOrElse(InternalServerError()))
      }
    }
  }
}
