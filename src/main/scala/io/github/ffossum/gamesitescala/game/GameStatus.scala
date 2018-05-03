package io.github.ffossum.gamesitescala.game

import cats.implicits._
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
  case object Canceled extends GameStatus {
    override val key: String = "canceled"
  }
  case object Ended extends GameStatus {
    override val key: String = "ended"
  }

  val values = List(NotStarted, InProgress, Canceled, Ended)

  def fromString(key: String): Option[GameStatus] = values.find(_.key === key)

  implicit val gameStatusEncoder: Encoder[GameStatus] = Encoder.encodeString.contramap(_.key)
  implicit val gameStatusDecoder: Decoder[GameStatus] = Decoder.decodeString.emap(str =>
    fromString(str).toRight(s"invalid ${GameStatus.getClass.getSimpleName} key $str"))

}
