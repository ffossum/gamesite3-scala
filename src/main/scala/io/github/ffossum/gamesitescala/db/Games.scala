package io.github.ffossum.gamesitescala.db

import cats.data.EitherT
import cats.effect.IO
import io.github.ffossum.gamesitescala.game.{Game, GameId, Timestamp}
import io.github.ffossum.gamesitescala.user.UserId
import cats.implicits._
import doobie.implicits._

class Games {}

object Games {
  def createGame(hostId: UserId): EitherT[IO, Throwable, Game] = {
    sql"INSERT INTO games (host_id) VALUES ($hostId)".update
      .withUniqueGeneratedKeys[(Timestamp, GameId)]("created_time", "id")
      .map({ case (createdTime, gameId) => Game(createdTime, hostId, gameId) })
      .transact(Database.xa)
      .attemptT
  }
}
