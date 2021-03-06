package io.github.ffossum.gamesitescala

import cats.Functor
import cats.data.{Kleisli, OptionT}
import cats.effect.Effect
import cats.implicits._
import io.circe.syntax._
import io.github.ffossum.gamesitescala.user.PrivateUserData
import org.http4s.{Cookie, HttpDate, Request, Response, headers}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import scala.language.higherKinds

object JwtMiddleware {
  val cookieName: String = "jwt"
  val key: String        = "secret"

  def authUserRequired[F[_]: Effect: Functor] = Kleisli { req: Request[F] =>
    OptionT(readJwtCookie(req).toOption.pure[F])
  }

  def authUserOptional[F[_]: Effect: Functor] = Kleisli { req: Request[F] =>
    OptionT.liftF(readJwtCookie(req).toOption.pure[F])
  }

  def readJwtCookie[F[_]: Effect](req: Request[F]): Either[String, PrivateUserData] = {
    for {
      header <- headers.Cookie.from(req.headers).toRight("cookie header not found")
      cookie <- header.values.toList.find(_.name === cookieName).toRight("token cookie not found")
      token = cookie.content
      userJson <- JwtCirce
        .decodeJson(token, key, List(JwtAlgorithm.HS256))
        .toEither
        .left
        .map(_.getMessage)
      user <- userJson.as[PrivateUserData].left.map(_.getMessage)
    } yield user
  }

  def setJwtCookie[F[_]: Effect](userData: PrivateUserData)(res: Response[F]): Response[F] = {
    val jwtString: String = JwtCirce.encode(userData.asJson, key, JwtAlgorithm.HS256)

    res.addCookie(
      Cookie(
        cookieName,
        jwtString,
        httpOnly = true,
        domain = None,
        path = Some("/"),
        expires = Some(HttpDate.MaxValue)
      )
    )
  }

  def removeJwtCookie[F[_]: Effect](res: Response[F]): Response[F] = res.removeCookie(cookieName)
}
