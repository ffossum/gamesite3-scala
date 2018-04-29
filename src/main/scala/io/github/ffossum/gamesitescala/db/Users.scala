package io.github.ffossum.gamesitescala.db

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import io.github.ffossum.gamesitescala.user._

import scala.language.higherKinds

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
  def createUser(
      username: Username,
      email: Email,
      password: Password,
  ): EitherT[IO, Throwable, DbUser] = {

    sql"insert into users (username, email, password_hash) values ($username, $email, ${password.hash})".update
      .withUniqueGeneratedKeys[DbUser]("id", "username", "email", "password_hash")
      .transact(Database.xa)
      .attemptT
  }

  def getUserById(userId: UserId): EitherT[IO, Throwable, DbUser] = {
    sql"SELECT id, username, email, password_hash FROM users WHERE id=$userId"
      .query[DbUser]
      .unique
      .transact(Database.xa)
      .attemptT
  }

  def getUserByEmail(email: Email): EitherT[IO, Throwable, DbUser] = {
    sql"SELECT id, username, email, password_hash FROM users WHERE email=$email"
      .query[DbUser]
      .unique
      .transact(Database.xa)
      .attemptT
  }
}
