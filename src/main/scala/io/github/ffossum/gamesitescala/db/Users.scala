package io.github.ffossum.gamesitescala.db

import scala.language.higherKinds
import cats.data.EitherT
import cats.effect.{Effect, IO}
import cats.implicits._
import io.github.ffossum.gamesitescala.user._

import scala.util.Random

case class DbUser(
    userId: UserId,
    username: Username,
    email: Email,
    passwordHash: PasswordHash
)
object DbUser {}

object Users {
  private val users: List[DbUser] = List(
    DbUser(UserId(1), Username("qwer"), Email("qwer@qwer.com"), PasswordHash("qwerqwer")),
    DbUser(UserId(2), Username("asdf"), Email("asdf@asdf.com"), PasswordHash("asdfasdf")),
    DbUser(UserId(3), Username("zxcv"), Email("zxcv@zxcv.com"), PasswordHash("zxcvzxcv")),
  )

  def createUser(
      username: Username,
      email: Email,
      password: Password,
  ): EitherT[IO, Throwable, DbUser] = {

    val dbUser = for {
      id <- IO(Random.nextLong)

      dbUser = DbUser(
        userId = UserId(id.abs),
        username = username,
        email = email,
        passwordHash = password.hash
      )
    } yield dbUser

    dbUser.attemptT

  }

  def getUserById(userId: UserId): EitherT[IO, Throwable, DbUser] = {
    val userOption = users.find(_.userId.value === userId.value)
    val userEither =
      userOption.toRight(new RuntimeException(s"User with user id ${userId.value} not found"))
    EitherT.fromEither[IO](userEither)
  }

  def getUserByEmail(email: Email): EitherT[IO, Throwable, DbUser] = {
    val userOption = users.find(_.email.value === email.value)
    val userEither =
      userOption.toRight(new RuntimeException(s"User with email ${email.value} not found"))
    EitherT.fromEither[IO](userEither)
  }
}
