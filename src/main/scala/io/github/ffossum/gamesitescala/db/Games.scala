package io.github.ffossum.gamesitescala.db

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import io.github.ffossum.gamesitescala.game.{Game, GameId, GameStatus, Timestamp}
import io.github.ffossum.gamesitescala.user.UserId

class Games {}

object Games {
  def createGame(hostId: UserId): EitherT[IO, Throwable, Game] = {
    sql"INSERT INTO games (host_id) VALUES ($hostId)".update
      .withUniqueGeneratedKeys[(Timestamp, GameId)]("created_time", "id")
      .map({ case (createdTime, gameId) => Game(createdTime, hostId, gameId) })
      .transact(Database.xa)
      .attemptT
  }

  def getGame(gameId: GameId): EitherT[IO, Throwable, Game] = {
    sql"SELECT created_time, host_id, other_players, game_status FROM games_view WHERE id=$gameId"
      .query[(Timestamp, UserId, List[Long], GameStatus)]
      .unique
      .map({
        case (createdTime, hostId, otherPlayers, gameStatus) =>
          val otherPlayerIdsSet = otherPlayers.map(UserId.apply).toSet
          Game(createdTime, hostId, gameId, otherPlayerIdsSet, gameStatus)
      })
      .transact(Database.xa)
      .attemptT
  }
}
