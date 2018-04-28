package io.github.ffossum.gamesitescala

import scala.language.higherKinds

import cats.data.Kleisli
import cats.effect.Effect
import io.circe.generic.auto._
import io.github.ffossum.gamesitescala.user.PrivateUserData
import org.http4s.util.CaseInsensitiveString
import org.http4s.{AttributeKey, HttpService, Request}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

object JwtMiddleware {
  val cookieName: String = "token"
  val key: String        = "secret-key"

  private val jwtAttributeKey: AttributeKey[PrivateUserData] = AttributeKey[PrivateUserData]

  def withUserFromJwt[F[_]: Effect](httpService: HttpService[F]): HttpService[F] = Kleisli { req =>
    val userOption = for {
      header   <- req.headers.get(CaseInsensitiveString(cookieName))
      token    <- Some(header.value)
      userJson <- JwtCirce.decodeJson(token, key, List(JwtAlgorithm.HS256)).toOption
      user     <- userJson.as[PrivateUserData].toOption
    } yield user

    userOption match {
      case Some(user) => httpService(req.withAttribute(jwtAttributeKey, user))
      case None       => httpService(req)
    }
  }

  def getUser[F[_]: Effect](req: Request[F]): Option[PrivateUserData] =
    req.attributes.get(jwtAttributeKey)
}
