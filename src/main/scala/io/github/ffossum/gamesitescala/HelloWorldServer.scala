package io.github.ffossum.gamesitescala

import cats.effect.{Effect, IO}
import cats.implicits._
import fs2.StreamApp
import io.deepstream.RpcResponse
import io.github.ffossum.gamesitescala.db.Games
import io.github.ffossum.gamesitescala.util.GsonSyntax._
import org.flywaydb.core.Flyway
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext
import scala.language.higherKinds
import org.log4s._
object HelloWorldServer extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val log = getLogger(HelloWorldServer.getClass)

  val flyway = new Flyway()
  flyway.setDataSource("jdbc:postgresql://172.17.0.2:5432/postgres", "postgres", "")
  flyway.migrate()

  def stream(args: List[String], requestShutdown: IO[Unit]) = {
    Deepstream.client.login(Deepstream.credentials)

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
  def loginService                    = new LoginService().service
  def registrationService             = new RegistrationService().service

  def stream(implicit ec: ExecutionContext) =
    BlazeBuilder[IO]
      .bindHttp(3000, "0.0.0.0")
      .mountService(loginService, "/api/login")
      .mountService(registrationService, "/api/registration")
      .mountService(helloWorldService, "/")
      .serve
}
