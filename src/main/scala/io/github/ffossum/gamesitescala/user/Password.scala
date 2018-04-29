package io.github.ffossum.gamesitescala.user

import io.circe._
import io.circe.generic.extras.semiauto._
import org.mindrot.jbcrypt.BCrypt.{checkpw, gensalt, hashpw}

import scala.util.Try

case class Password(value: String) extends AnyVal
object Password {
  implicit val passwordDecoder: Decoder[Password] = deriveUnwrappedDecoder
  implicit val passwordEncoder
    : Encoder[Password] = Encoder.instance(_ => Json.Null) // password should never be exposed to the outside

  implicit class HashablePassword(x: Password) {
    lazy val hash: PasswordHash = PasswordHash(hashpw(x.value, gensalt))
  }
}

case class PasswordHash(value: String) extends AnyVal
object PasswordHash {
  implicit val passwordHashEncoder: Encoder[Password] =
    Encoder.instance(_ => Json.Null) // password hash should never be exposed to the outside

  def checkPassword(password: Password, passwordHash: PasswordHash): Boolean =
    Try(checkpw(password.value, passwordHash.value)).getOrElse(false)
}
