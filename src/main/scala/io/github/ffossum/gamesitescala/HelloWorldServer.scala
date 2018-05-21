package io.github.ffossum.gamesitescala

import cats.effect.{Effect, IO}
import cats.implicits._
import com.google.gson.JsonElement
import fs2.StreamApp
import io.deepstream.RpcResponse
import io.github.ffossum.gamesitescala.db.Games
import io.github.ffossum.gamesitescala.deepstream.Channel
import io.github.ffossum.gamesitescala.deepstream.Channel.LOBBY
import io.github.ffossum.gamesitescala.game.GameStatus
import io.github.ffossum.gamesitescala.util.GsonSyntax._
import org.flywaydb.core.Flyway
import org.http4s.server.blaze.BlazeBuilder
import org.log4s._

import scala.concurrent.ExecutionContext
import scala.language.higherKinds
object HelloWorldServer extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val log = getLogger(HelloWorldServer.getClass)

  val flyway = new Flyway()
  flyway.setDataSource("jdbc:postgresql://172.17.0.2:5432/postgres", "postgres", "")
  flyway.migrate()

  def stream(args: List[String], requestShutdown: IO[Unit]) = {
    Deepstream.client.login(Deepstream.credentials)

    Deepstream.client.rpc.provide(
      "create-game",
      (s: String, o: Any, rpcResponse: RpcResponse) => {
        val resultIO = for {
          json          <- IO(o.asInstanceOf[JsonElement]).attemptT
          createGameReq <- json.as[CreateGameReq].toEitherT[IO]
          game          <- Games.createGame(createGameReq.uid)
          _ <- IO(
            Deepstream.client.event
              .emit(LOBBY.eventName, DeepstreamEvent("create-game", game).asGson)).attemptT
          _ <- IO(rpcResponse.send(game.asGson)).attemptT

        } yield ()

        resultIO.value.unsafeRunSync match {
          case Left(t) => log.error(t)("error while creating game")
          case _       => ()
        }
      }
    )
    Deepstream.client.rpc.provide(
      "refresh-lobby",
      (s: String, o: Any, rpcResponse: RpcResponse) => {
        val resultIO = for {
          games  <- Games.getLobbyGames
          result <- IO(rpcResponse.send(games.asGson)).attemptT
        } yield result

        resultIO.value.unsafeRunSync match {
          case Left(t) => log.error(t)("error while refreshing lobby")
          case _       => ()
        }
      }
    )
    Deepstream.client.rpc.provide(
      "join-game",
      (s: String, o: Any, rpcResponse: RpcResponse) => {
        val resultIO = for {
          json         <- IO(o.asInstanceOf[JsonElement]).attemptT
          gamePlayerId <- json.as[DeepstreamGamePlayerId].toEitherT[IO]
          game         <- Games.addPlayer(gamePlayerId.gid, gamePlayerId.uid)
          _            <- IO(rpcResponse.send(game.asGson)).attemptT
          _ <- IO(
            Deepstream.client.event.emit(LOBBY.eventName,
                                         DeepstreamEvent("game-updated",
                                                         GameUpdate(
                                                           id = game.id,
                                                           players = Some(game.players)
                                                         )))
          ).attemptT

          channels = game.players.map(Channel.user) + Channel.spectate(game.id)
          _ <- channels.toList
            .traverse_[IO, Unit](channel =>
              IO(Deepstream.client.event.emit(channel.eventName,
                                              DeepstreamEvent("player-joined", gamePlayerId))))
            .attemptT

        } yield ()

        resultIO.value.unsafeRunSync match {
          case Left(t) => log.error(t)("error while joining game")
          case _       => ()
        }

      }
    )
    Deepstream.client.rpc.provide(
      "leave-game",
      (s: String, o: Any, rpcResponse: RpcResponse) => {
        val resultIO = for {
          json         <- IO(o.asInstanceOf[JsonElement]).attemptT
          gamePlayerId <- json.as[DeepstreamGamePlayerId].toEitherT[IO]
          game         <- Games.removePlayer(gamePlayerId.gid, gamePlayerId.uid)
          _            <- IO(rpcResponse.send(game.asGson)).attemptT
          _ <- IO(
            Deepstream.client.event.emit(LOBBY.eventName,
                                         DeepstreamEvent("game-updated",
                                                         GameUpdate(
                                                           id = game.id,
                                                           players = Some(game.players)
                                                         )))
          ).attemptT
          channels = game.players.map(Channel.user) + Channel.user(gamePlayerId.uid) + Channel
            .spectate(game.id)
          _ <- channels.toList
            .traverse_[IO, Unit](channel =>
              IO(Deepstream.client.event.emit(channel.eventName,
                                              DeepstreamEvent("player-joined", gamePlayerId))))
            .attemptT
        } yield ()

        resultIO.value.unsafeRunSync match {
          case Left(t) => log.error(t)("error while leaving game")
          case _       => ()
        }
      }
    )
    Deepstream.client.rpc
      .provide(
        "cancel-game",
        (s: String, o: Any, rpcResponse: RpcResponse) => {
          val resultIO = for {
            json         <- IO(o.asInstanceOf[JsonElement]).attemptT
            gamePlayerId <- json.as[DeepstreamGamePlayerId].toEitherT[IO]

            requestedBy = gamePlayerId.uid
            gameId      = gamePlayerId.gid

            game <- Games.cancelGame(gameId, requestedBy)
            _    <- IO(rpcResponse.send(game.status == GameStatus.Canceled)).attemptT
            _ <- IO(
              Deepstream.client.event.emit(LOBBY.eventName,
                                           DeepstreamEvent("game-updated",
                                                           GameUpdate(
                                                             id = game.id,
                                                             status = Some(game.status)
                                                           )))
            ).attemptT
            channels = game.players.map(Channel.user) + Channel.spectate(game.id)
            _ <- channels.toList
              .traverse_[IO, Unit](channel =>
                IO(Deepstream.client.event.emit(channel.eventName,
                                                DeepstreamEvent("game-canceled", gamePlayerId))))
              .attemptT
          } yield ()

          resultIO.value.unsafeRunSync match {
            case Left(t) => log.error(t)("error while canceling game")
            case _       => ()
          }
        }
      )
    Deepstream.client.rpc
      .provide(
        "start-game",
        (s: String, o: Any, rpcResponse: RpcResponse) => {
          val resultIO = for {
            json         <- IO(o.asInstanceOf[JsonElement]).attemptT
            gamePlayerId <- json.as[DeepstreamGamePlayerId].toEitherT[IO]

            requestedBy = gamePlayerId.uid
            gameId      = gamePlayerId.gid

            game <- Games.startGame(gameId, requestedBy)
            _    <- IO(rpcResponse.send(game.status == GameStatus.InProgress)).attemptT
            _ <- IO(
              Deepstream.client.event.emit(LOBBY.eventName,
                                           DeepstreamEvent("game-updated",
                                                           GameUpdate(
                                                             id = game.id,
                                                             status = Some(game.status)
                                                           )))
            ).attemptT
            channels = game.players.map(Channel.user) + Channel.spectate(game.id)
            _ <- channels.toList
              .traverse_[IO, Unit](channel =>
                IO(Deepstream.client.event.emit(channel.eventName,
                                                DeepstreamEvent("game-started", gamePlayerId))))
              .attemptT
          } yield ()

          resultIO.value.unsafeRunSync match {
            case Left(t) => log.error(t)("error while starting game")
            case _       => ()
          }
        }
      )

    ServerStream.stream
  }
}

object ServerStream {

  def helloWorldService[F[_]: Effect] = new HelloWorldService[F].service
  def apiService                      = new ApiService().service
  def loginService                    = new LoginService().service
  def registrationService             = new RegistrationService().service

  def stream(implicit ec: ExecutionContext) =
    BlazeBuilder[IO]
      .bindHttp(3000, "0.0.0.0")
      .mountService(loginService, "/api/login")
      .mountService(registrationService, "/api/registration")
      .mountService(apiService, "/api")
      .mountService(helloWorldService, "/")
      .serve
}
