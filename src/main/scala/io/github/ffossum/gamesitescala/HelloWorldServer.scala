package io.github.ffossum.gamesitescala

import cats.effect.{Effect, IO}
import fs2.StreamApp
import org.flywaydb.core.Flyway
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

object HelloWorldServer extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  val flyway = new Flyway()
  flyway.setDataSource("jdbc:postgresql://172.17.0.2:5432/postgres", "postgres", "")
  flyway.migrate()

  def stream(args: List[String], requestShutdown: IO[Unit]) = {
    Deepstream.client.login(Deepstream.credentials)
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
