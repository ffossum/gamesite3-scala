package io.github.ffossum.gamesitescala

import scala.language.higherKinds

import cats.effect.{Effect, IO}
import fs2.StreamApp
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object HelloWorldServer extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]) = ServerStream.stream
}

object ServerStream {

  def helloWorldService[F[_]: Effect] = new HelloWorldService[F].service
  def loginService                    = new LoginService().service

  def stream(implicit ec: ExecutionContext) =
    BlazeBuilder[IO]
      .bindHttp(3000, "0.0.0.0")
      .mountService(helloWorldService, "/")
      .mountService(loginService, "/login")
      .serve
}
