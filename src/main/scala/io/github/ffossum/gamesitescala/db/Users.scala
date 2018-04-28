package io.github.ffossum.gamesitescala.db

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import io.github.ffossum.gamesitescala.user._

import scala.language.higherKinds
import scala.util.Random

case class DbUser(
    userId: UserId,
    username: Username,
    email: Email,
    passwordHash: PasswordHash
) {
  def toPrivateUserData: PrivateUserData = PrivateUserData(userId, email, username)
}
object DbUser {}

object Users {
  private val users: List[DbUser] = List(
    DbUser(
      UserId(1),
      Username("qwer"),
      Email("qwer@qwer.com"),
      PasswordHash("$2a$10$NozLFepV2flysA98NoWde.QU6nKII3fYe7L7ZpXdNAQ8M8RnwttZm")
    ),
    DbUser(
      UserId(2),
      Username("asdf"),
      Email("asdf@asdf.com"),
      PasswordHash("$2a$10$Gat0BzSmxw8a9gAq/9PgpOum5iaiM8S5Dq0bYZ7Kcf9i1IJAPQLFm")
    ),
    DbUser(
      UserId(3),
      Username("zxcv"),
      Email("zxcv@zxcv.com"),
      PasswordHash("$2a$10$WJvHFVlPf66GXmXFCFk.k.0Ci7N1OQH8XuM7TMxeVdvM0tg0j/TTy")
    ),
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
