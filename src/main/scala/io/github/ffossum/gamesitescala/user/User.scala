package io.github.ffossum.gamesitescala.user

import cats.Eq
import cats.implicits._
import io.circe._
import io.circe.generic.extras.semiauto.{deriveUnwrappedDecoder, deriveUnwrappedEncoder}
import io.circe.generic.semiauto._

import scala.util.Try

case class Email(value: String) extends AnyVal
object Email {
  implicit val emailDecoder: Decoder[Email] = deriveUnwrappedDecoder
  implicit val emailEncoder: Encoder[Email] = deriveUnwrappedEncoder
}

case class Username(value: String) extends AnyVal
object Username {
  implicit val usernameDecoder: Decoder[Username] = deriveUnwrappedDecoder
  implicit val usernameEncoder: Encoder[Username] = deriveUnwrappedEncoder
}

case class UserId(value: Int) extends AnyVal
object UserId {
  implicit val userIdDecoder: Decoder[UserId] =
    Decoder[String].emapTry(str => Try(UserId(str.toInt)))
  implicit val userIdEncoder: Encoder[UserId] =
    Encoder[String].contramap(_.value.toString)

  implicit val userIdEq: Eq[UserId] = Eq.instance((a, b) => a.value === b.value)
}

case class PrivateUserData(
    id: UserId,
    email: Email,
    username: Username
)
object PrivateUserData {
  implicit val privateUserDataDecoder: Decoder[PrivateUserData] = deriveDecoder
  implicit val privateUserDataEncoder: Encoder[PrivateUserData] = deriveEncoder
}

case class PublicUserData(
    id: UserId,
    username: Username
)

object PublicUserData {
  implicit val decoder: Decoder[PublicUserData] = deriveDecoder
  implicit val encoder: Encoder[PublicUserData] = deriveEncoder
}
