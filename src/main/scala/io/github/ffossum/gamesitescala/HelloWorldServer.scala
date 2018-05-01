package io.github.ffossum.gamesitescala

import cats.data.EitherT
import cats.effect.{Effect, IO}
import cats.implicits._
import fs2.StreamApp
import io.circe.parser.parse
import io.deepstream.RpcResponse
import io.github.ffossum.gamesitescala.db.Games
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
        val game = for {
          json          <- EitherT.fromEither[IO](parse(o.toString))
          createGameReq <- json.as[CreateGameReq].toEitherT[IO]
          game          <- Games.createGame(createGameReq.uid)
          _ <- IO(
            Deepstream.client.event
              .emit("lobby", DeepstreamEvent("create-game", game).asGson)).attemptT
          _ <- IO(rpcResponse.send(game.asGson)).attemptT

        } yield ()

        game.value.unsafeRunSync match {
          case Left(t) => log.error(t)("failed to create game")
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
          case Left(t) => log.error(t)("failed to refresh lobby")
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
