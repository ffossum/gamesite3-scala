package io.github.ffossum.gamesitescala.user

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
  implicit val usernameDecoder: Decoder[UserId] =
    Decoder.decodeString.emapTry(str => Try(UserId(str.toInt)))
  implicit val usernameEncoder: Encoder[UserId] =
    Encoder.encodeString.contramap(_.value.toString)
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
