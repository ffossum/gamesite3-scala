package io.github.ffossum.gamesitescala

import cats.effect.Effect
import org.http4s.HttpService
import org.http4s.MediaType.`text/html`
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`

class HelloWorldService[F[_]: Effect] extends Http4sDsl[F] {

  val service: HttpService[F] = {
    HttpService[F] {
      case GET -> _ =>
        Ok(
          s"""<!doctype html>
             |<html lang="en">
             |<head>
             |  <meta charset="utf-8">
             |  <title>Gamesite 3</title>
             |  <link rel="shortcut icon" href="data:image/x-icon;," type="image/x-icon" />
             |
             |  <script src="//localhost:8080/scripts/bundle.js" defer></script>
             |</head>
             |
             |<body>
             |  <div id="root"></div>
             |</body>
             |
             |</html>
           """.stripMargin,
          `Content-Type`(`text/html`)
        )
    }
  }
}
