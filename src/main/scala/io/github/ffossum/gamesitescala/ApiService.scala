package io.github.ffossum.gamesitescala
import cats.data.Validated.Valid
import cats.data.{EitherT, NonEmptyList}
import cats.effect.IO
import cats.implicits._
import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.github.ffossum.gamesitescala.db.{Games, Users}
import io.github.ffossum.gamesitescala.game.{Game, GameId}
import io.github.ffossum.gamesitescala.user._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware

import scala.language.higherKinds
import scala.util.Try

case class GamePlayerId(gameId: GameId, userId: UserId)
object GamePlayerId {
  implicit val decoder: Decoder[GamePlayerId]                 = deriveDecoder
  implicit val entityDecoder: EntityDecoder[IO, GamePlayerId] = jsonOf[IO, GamePlayerId]
}

class ApiService extends Http4sDsl[IO] {
  implicit val userIdQueryParam: QueryParamDecoder[UserId] =
    QueryParamDecoder[Int].map(UserId.apply)
  implicit val gameIdQueryParam: QueryParamDecoder[GameId] =
    QueryParamDecoder[Int].map(GameId.apply)

  object UserIdsMatcher extends OptionalMultiQueryParamDecoderMatcher[UserId]("id")

  object GameIdVar {
    def unapply(str: String): Option[GameId] = Try(GameId(str.toInt)).toOption
  }

  val nonAuthedService: HttpService[IO] =
    HttpService {
      case GET -> Root / "users" :? UserIdsMatcher(userIdsMatch) => {
        userIdsMatch match {
          case Valid(head :: tail) => {
            val userIds = NonEmptyList(head, tail)

            Users
              .getUsersByIds(userIds)
              .value
              .flatMap({
                case Right(users) => Ok(users.map(_.toPublicUserData).asJson)
                case Left(_)      => InternalServerError("")
              })
          }

          case _ => BadRequest()
        }
      }
      case GET -> Root / "game" / GameIdVar(gameId) => {
        Games
          .getGame(gameId)
          .value
          .flatMap({
            case Right(game) => Ok(game.asJson)
            case _           => NotFound()
          })
      }
    }

  val authedService: AuthedService[PrivateUserData, IO] =
    AuthedService {
      case authedReq @ POST -> Root / "game" / "join" as user => {

        val result: EitherT[IO, Status, Game] = for {
          gamePlayerId <- authedReq.req
            .as[GamePlayerId]
            .attemptT
            .leftMap(_ => Status.BadRequest)
          game <- if (gamePlayerId.userId === user.id)
            EitherT.fromEither[IO](Left(Status.Forbidden))
          else
            Games
              .addPlayer(gamePlayerId.gameId, gamePlayerId.userId)
              .leftMap(_ => Status.InternalServerError)
        } yield game

        result.value.flatMap({
          case Right(game)  => Ok(game.asJson)
          case Left(status) => IO(Response(status))
        })

      }
    }

  private val middleware: AuthMiddleware[IO, PrivateUserData] =
    AuthMiddleware(JwtMiddleware.authUserRequired)

  val service: HttpService[IO] = nonAuthedService.combineK(middleware(authedService))

}
