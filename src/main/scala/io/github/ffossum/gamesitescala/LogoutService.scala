package io.github.ffossum.gamesitescala

import cats.effect.IO
import io.github.ffossum.gamesitescala.JwtMiddleware._
import org.http4s._
import org.http4s.dsl.Http4sDsl

import scala.language.higherKinds

class LogoutService extends Http4sDsl[IO] {
  val service: HttpService[IO] = {
    HttpService[IO] {
      case GET -> Root =>
        SeeOther(uri("/")).map(removeJwtCookie(_))
    }
  }
}
