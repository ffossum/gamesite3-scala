package io.github.ffossum.gamesitescala.deepstream

import io.github.ffossum.gamesitescala.game.GameId
import io.github.ffossum.gamesitescala.user.UserId

case class Channel(eventName: String) extends AnyVal

object Channel {
  val LOBBY                             = Channel("lobby")
  def user(userId: UserId): Channel     = Channel(s"user:${userId.value}")
  def spectate(gameId: GameId): Channel = Channel(s"spectate:${gameId.value}")
}
