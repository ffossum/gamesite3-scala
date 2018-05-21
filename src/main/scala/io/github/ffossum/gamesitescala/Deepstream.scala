package io.github.ffossum.gamesitescala
import com.google.gson.JsonObject
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import io.deepstream._
import io.github.ffossum.gamesitescala.game.{GameId, GameStatus}
import io.github.ffossum.gamesitescala.user.UserId

import scala.language.higherKinds

case class DeepstreamEvent[+Payload](t: String, p: Payload)
object DeepstreamEvent {
  implicit def encoder[Payload: Encoder]: Encoder[DeepstreamEvent[Payload]] = deriveEncoder
}

case class CreateGameReq(uid: UserId)
object CreateGameReq {
  implicit val decoder: Decoder[CreateGameReq] = deriveDecoder
}
case class DeepstreamGamePlayerId(uid: UserId, gid: GameId)
object DeepstreamGamePlayerId {
  implicit val decoder: Decoder[DeepstreamGamePlayerId] = deriveDecoder
  implicit val encoder: Encoder[DeepstreamGamePlayerId] = deriveEncoder
}

case class GameUpdate(
    id: GameId,
    host: Option[UserId] = None,
    players: Option[Set[UserId]] = None,
    status: Option[GameStatus] = None
)
object GameUpdate {
  implicit val decoder: Decoder[GameUpdate] = deriveDecoder
  implicit val encoder: Encoder[GameUpdate] = deriveEncoder
}

object Deepstream {
  private val DEEPSTREAM_USERNAME = "secret server username"
  private val DEEPSTREAM_PASSWORD = "secret deepstream password"

  val client = new DeepstreamClient("localhost:6020")

  val credentials: JsonObject = {
    val obj = new JsonObject()
    obj.addProperty("username", DEEPSTREAM_USERNAME)
    obj.addProperty("password", DEEPSTREAM_PASSWORD)
    obj
  }

}
