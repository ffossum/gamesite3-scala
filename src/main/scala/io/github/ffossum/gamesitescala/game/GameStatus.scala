package io.github.ffossum.gamesitescala.game

import cats.implicits._
import doobie.util.meta.Meta
import io.circe.{Decoder, Encoder}

sealed trait GameStatus {
  def key: String
}

object GameStatus {
  case object NotStarted extends GameStatus {
    override val key: String = "not_started"
  }
  case object InProgress extends GameStatus {
    override val key: String = "in_progress"
  }

  val values = List(NotStarted, InProgress)

  def fromString(key: String): Option[GameStatus] = values.find(_.key === key)

  implicit val gameStatusEncoder: Encoder[GameStatus] = Encoder.encodeString.contramap(_.key)
  implicit val gameStatusDecoder: Decoder[GameStatus] = Decoder.decodeString.emap(str =>
    fromString(str).toRight(s"invalid ${GameStatus.getClass.getSimpleName} key $str"))

  implicit val gameStatusMeta: Meta[GameStatus] = Meta.StringMeta.xmap(unsafeFromString, _.key)
  def unsafeFromString(s: String): GameStatus =
    fromString(s).getOrElse(throw doobie.util.invariant.InvalidEnum[GameStatus](s))
}
