package io.github.ffossum.gamesitescala

import cats.data.NonEmptyList
import cats.data.Validated.Valid
import cats.effect.IO
import io.circe.syntax._
import io.github.ffossum.gamesitescala.db.{Games, Users}
import io.github.ffossum.gamesitescala.game.GameId
import io.github.ffossum.gamesitescala.user._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import scala.language.higherKinds
import scala.util.Try

class ApiService extends Http4sDsl[IO] {
  implicit val userIdQueryParam: QueryParamDecoder[UserId] =
    QueryParamDecoder[Int].map(UserId.apply)
  implicit val gameIdQueryParam: QueryParamDecoder[GameId] =
    QueryParamDecoder[Int].map(GameId.apply)

  object UserIdsMatcher extends OptionalMultiQueryParamDecoderMatcher[UserId]("id")

  object GameIdVar {
    def unapply(str: String): Option[GameId] = Try(GameId(str.toInt)).toOption
  }

  val service: HttpService[IO] = {
    HttpService[IO] {
      case GET -> Root / "users" :? UserIdsMatcher(userIdsMatch) => {
        userIdsMatch match {
          case Valid(head :: tail) => {
            val userIds = NonEmptyList(head, tail)
            val data = Users
              .getUsersByIds(userIds)
              .value
              .unsafeRunSync
              .getOrElse(Nil)
              .map(_.toPublicUserData)
              .asJson

            Ok(data)
          }

          case _ => BadRequest()
        }
      }
      case GET -> Root / "game" / GameIdVar(gameId) => {
        val result = Games.getGame(gameId).value.unsafeRunSync()
        result match {
          case Right(game) => Ok(game.asJson)
          case _           => NotFound()
        }
      }
      case _ => NotFound()
    }
  }
}
