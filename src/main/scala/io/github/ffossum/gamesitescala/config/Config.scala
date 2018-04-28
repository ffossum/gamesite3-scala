package io.github.ffossum.gamesitescala.config

import java.net.URL
import cats.implicits._

import cats.data.Validated._
import cats.data.{NonEmptyList, Validated, ValidatedNel}

case class DeepstreamHost(url: URL)             extends AnyVal
case class DeepstreamUsername(username: String) extends AnyVal
case class DeepstreamPassword(password: String) extends AnyVal
case class JwtSecret(secret: String)            extends AnyVal
case class PostgresHost(url: URL)               extends AnyVal
case class HashidSalt(salt: String)             extends AnyVal

case class Config(
    deepstreamHost: DeepstreamHost,
    deepstreamUsername: DeepstreamUsername,
    deepstreamPassword: DeepstreamPassword,
    jwtSecret: JwtSecret,
)

private object ConfigKeys {
  val DEEPSTREAM_HOST     = "DEEPSTREAM_HOST"
  val DEEPSTREAM_USERNAME = "DEEPSTREAM_USERNAME"
  val DEEPSTREAM_PASSWORD = "DEEPSTREAM_PASSWORD"
  val JWT_SECRET          = "JWT_SECRET"
  val POSTGRES_HOST       = "POSTGRES_HOST"
}

object Config {
  import ConfigKeys._

  implicit val globalConfig: Config = validateConfig(sys.env).right.get

  type Env             = Map[String, String]
  type ConfigResult[A] = ValidatedNel[String, A]

  def validateConfig(env: Env): Either[String, Config] = {
    (validateDeepstreamHost(env),
     validateDeepstreamUsername(env),
     validateDeepstreamPassword(env),
     validateJwtSecret(env))
      .mapN[Config](Config.apply)
      .toEither
      .leftMap(_.toList.mkString(", "))
  }

  private def validateDeepstreamHost(env: Env): ConfigResult[DeepstreamHost] = {
    val url: Either[String, DeepstreamHost] = env
      .get(DEEPSTREAM_HOST)
      .toRight(s"$DEEPSTREAM_HOST was missing")
      .flatMap(url => Either.catchNonFatal(DeepstreamHost(new URL(url))).leftMap(_.getMessage))

    Validated.fromEither(url).leftMap(NonEmptyList.one)
  }
  private def validatePostgres(env: Env): ConfigResult[PostgresHost] = {
    val url: Either[String, PostgresHost] = env
      .get(POSTGRES_HOST)
      .toRight(s"$DEEPSTREAM_HOST was missing")
      .flatMap(url => Either.catchNonFatal(PostgresHost(new URL(url))).leftMap(_.getMessage))

    Validated.fromEither(url).leftMap(NonEmptyList.one)
  }

  private def validateDeepstreamUsername(env: Env): ConfigResult[DeepstreamUsername] = {
    val username =
      env
        .get(DEEPSTREAM_USERNAME)
        .map(DeepstreamUsername)
        .toRight(NonEmptyList.one(s"$DEEPSTREAM_USERNAME was missing"))

    Validated.fromEither(username)
  }

  private def validateDeepstreamPassword(env: Env): ConfigResult[DeepstreamPassword] = {
    val password = env
      .get(DEEPSTREAM_PASSWORD)
      .map(DeepstreamPassword)
      .toRight(NonEmptyList.one(s"$DEEPSTREAM_PASSWORD was missing"))

    Validated.fromEither(password)
  }

  private def validateJwtSecret(env: Env): ConfigResult[JwtSecret] = {
    val secret = env
      .get(JWT_SECRET)
      .map(JwtSecret)
      .toRight(NonEmptyList.one(s"$JWT_SECRET was missing"))

    Validated.fromEither(secret)
  }
}
