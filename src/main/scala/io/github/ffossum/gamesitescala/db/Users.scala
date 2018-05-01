package io.github.ffossum.gamesitescala.db

import cats.data.{EitherT, NonEmptyList}
import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.meta.Meta
import io.github.ffossum.gamesitescala.user._

import scala.language.higherKinds

case class DbUser(
    userId: UserId,
    username: Username,
    email: Email,
    passwordHash: PasswordHash
) {
  def toPrivateUserData: PrivateUserData = PrivateUserData(userId, email, username)
  def toPublicUserData: PublicUserData   = PublicUserData(userId, username)
}
object DbUser {}

object Users {

  implicit val userIdListMeta: Meta[List[UserId]] =
    Meta[Array[Int]].xmap(_.toList.map(UserId.apply), _.map(_.value).toArray)

  def createUser(
      username: Username,
      email: Email,
      password: Password,
  ): EitherT[IO, Throwable, DbUser] = {

    sql"INSERT INTO users (username, email, password_hash) VALUES ($username, $email, ${password.hash})".update
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

  def getUsersByIds(userIds: NonEmptyList[UserId]): EitherT[IO, Throwable, List[DbUser]] = {

    (fr"""
       SELECT id, username, email, password_hash
       FROM users
       WHERE """ ++ Fragments.in(fr"id", userIds) ++ fr"""
       LIMIT ${userIds.length}
      """)
      .query[DbUser]
      .to[List]
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
