package io.github.ffossum.gamesitescala

import cats.effect.IO
import cats.implicits._
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import io.github.ffossum.gamesitescala.db.Users
import io.github.ffossum.gamesitescala.user.{Email, Password, PasswordHash}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import JwtMiddleware._

import scala.language.higherKinds

case class Login(email: Email, password: Password)
object Login {
  implicit val loginDecoder: Decoder[Login] = deriveDecoder
  implicit val loginEncoder: Encoder[Login] = deriveEncoder

  implicit val loginEntityDecoder: EntityDecoder[IO, Login] = jsonOf[IO, Login]
}

class LoginService extends Http4sDsl[IO] {
  val service: HttpService[IO] = {
    HttpService[IO] {
      case req @ POST -> Root =>
        val res = for {
          login <- req.as[Login].attemptT
          user  <- Users.getUserByEmail(login.email)
          validPassword   = PasswordHash.checkPassword(login.password, user.passwordHash)
          privateUserData = user.toPrivateUserData
        } yield
          if (validPassword)
            Ok(privateUserData.asJson).map(setJwtCookie(privateUserData))
          else
            BadRequest("login failed")

        res.value.flatMap(_.getOrElse(InternalServerError()))
    }
  }
}
