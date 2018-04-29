package io.github.ffossum.gamesitescala.game

import io.github.ffossum.gamesitescala.user.UserId
import io.circe._
import io.circe.generic.semiauto._

import scala.util.Try

case class GameId(value: Long) extends AnyVal
object GameId {
  implicit val gameIdDecoder: Decoder[GameId] =
    Decoder.decodeString.emapTry(str => Try(GameId(str.toLong)))
  implicit val gameIdEncoder: Encoder[GameId] =
    Encoder.encodeString.contramap(_.value.toString)
}

case class Game(
    createdTime: Timestamp,
    host: UserId,
    id: GameId,
    players: Set[UserId],
    status: GameStatus
)
object Game {
  implicit val gameDecoder: Decoder[Game] = deriveDecoder
  implicit val gameEncoder: Encoder[Game] = deriveEncoder
}
