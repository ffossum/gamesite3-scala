package io.github.ffossum.gamesitescala

import cats.effect.Effect
import io.circe.syntax._
import io.github.ffossum.gamesitescala.user.PrivateUserData
import org.http4s.MediaType.`text/html`
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedService, HttpService}

import scala.language.higherKinds

class HelloWorldService[F[_]: Effect] extends Http4sDsl[F] {

  private val middleware: AuthMiddleware[F, Option[PrivateUserData]] =
    AuthMiddleware.withFallThrough(JwtMiddleware.authUserOptional)

  val authedService: AuthedService[Option[PrivateUserData], F] =
    AuthedService {
      case GET -> _ as Some(user) => indexHtml(initialStateScript(user))
      case GET -> _ as None       => indexHtml()
    }

  val service: HttpService[F] = middleware(authedService)

  def initialStateScript(user: PrivateUserData) = {
    val userJson = user.asJson
    s"""<script defer>window.__USER__ = ${userJson.noSpaces};</script>"""
  }

  def indexHtml(initialStateScript: String = "") = Ok(
    s"""<!doctype html>
       |<html lang="en">
       |<head>
       |  <meta charset="utf-8">
       |  <title>Gamesite 3</title>
       |  <link rel="shortcut icon" href="data:image/x-icon;," type="image/x-icon" />
       |  $initialStateScript
       |  <script src="//localhost:8080/scripts/bundle.js" defer></script>
       |</head>
       |
       |<body>
       |  <div id="root"></div>
       |</body>
       |
       |</html>""".stripMargin,
    `Content-Type`(`text/html`)
  )
}
