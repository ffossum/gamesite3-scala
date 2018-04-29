package io.github.ffossum.gamesitescala.game

import io.circe.{Decoder, Encoder}
import cats.implicits._

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

}