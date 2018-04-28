package io.github.ffossum.gamesitescala.user

import cats.implicits._
import io.circe._
import io.circe.generic.extras.semiauto._

case class Password(value: String) extends AnyVal
object Password {
  implicit val emailDecoder: Decoder[Password] = deriveUnwrappedDecoder
  implicit val emailEncoder: Encoder[Password] = deriveUnwrappedEncoder

  implicit class HashablePassword(x: Password) {
    lazy val hash: PasswordHash = PasswordHash(x.value) // TODO add real hash function (bcrypt?)
  }
}

case class PasswordHash(value: String) extends AnyVal
object PasswordHash {
  def checkPassword(passwordHash: PasswordHash, password: Password): Boolean =
    passwordHash.value === password.hash.value
}
