package io.github.ffossum.gamesitescala.game

import java.time.Instant

import cats.effect.Effect
import cats.implicits._
import io.circe._

import scala.language.higherKinds
import scala.util.Try

case class Timestamp(value: Instant) extends AnyVal
object Timestamp {
  def now[F[_]: Effect]: F[Timestamp] = Timestamp(Instant.now).pure[F]
  def parse(str: CharSequence): Try[Timestamp] =
    Try(Instant.parse(str)).map(Timestamp.apply)

  implicit val timestampEncoder: Encoder[Timestamp] =
    Encoder.encodeString.contramap(_.value.toString)
  implicit val timestampDecoder: Decoder[Timestamp] =
    Decoder.decodeString.emapTry(parse)
}
