package io.github.ffossum.gamesitescala

import cats.data.NonEmptyList
import cats.data.Validated.Valid
import cats.effect.IO
import io.circe.syntax._
import io.github.ffossum.gamesitescala.db.Users
import io.github.ffossum.gamesitescala.user._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import cats.implicits._

import scala.language.higherKinds

class ApiService extends Http4sDsl[IO] {
  implicit val userIdQueryParam: QueryParamDecoder[UserId] =
    QueryParamDecoder[Int].map(UserId.apply)

  object UserIdsMatcher extends OptionalMultiQueryParamDecoderMatcher[UserId]("id")

  val service: HttpService[IO] = {
    HttpService[IO] {
      case GET -> Root / "users" :? UserIdsMatcher(userIdsMatch) => {
        userIdsMatch match {
          case Valid(Nil) => BadRequest("no user ids specified")
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
    }
  }
}
