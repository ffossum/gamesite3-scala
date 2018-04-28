package io.github.ffossum.gamesitescala.user

import io.circe._
import io.circe.generic.extras.semiauto._

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

case class UserId(value: Long) extends AnyVal
object UserId {
  implicit val usernameDecoder: Decoder[UserId] = deriveUnwrappedDecoder
  implicit val usernameEncoder: Encoder[UserId] = deriveUnwrappedEncoder
}

case class PrivateUserData(
    id: UserId,
    email: Email,
    username: Username
)

case class PublicUserData(
    id: UserId,
    username: Username
)
