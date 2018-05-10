package io.github.ffossum.gamesitescala.db

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.meta.Meta
import io.github.ffossum.gamesitescala.game.GameStatus._
import io.github.ffossum.gamesitescala.game.{Game, GameId, GameStatus, Timestamp}
import io.github.ffossum.gamesitescala.user.UserId

object Games {
  implicit val gameStatusMeta: Meta[GameStatus] =
    pgEnumStringOpt("game_status", GameStatus.fromString, _.key)

  implicit val userIdSetMeta: Meta[Set[UserId]] =
    Meta[Array[Int]].xmap(_.toSet.map(UserId.apply), _.map(_.value).toArray)

  def createGame(hostId: UserId): EitherT[IO, Throwable, Game] = {
    sql"INSERT INTO games (host_id) VALUES ($hostId)".update
      .withUniqueGeneratedKeys[(Timestamp, GameId)]("created_time", "id")
      .map({ case (createdTime, gameId) => Game(createdTime, hostId, gameId) })
      .transact(Database.xa)
      .attemptT
  }

  def getLobbyGames: EitherT[IO, Throwable, List[Game]] = {
    val gameStatus: GameStatus = GameStatus.NotStarted
    sql"""
       SELECT id, created_time, host_id, other_players
       FROM games_view
       WHERE game_status=$gameStatus"""
      .query[(GameId, Timestamp, UserId, Set[UserId])]
      .to[List]
      .map(_.map {
        case (gameId, createdTime, hostId, otherPlayers) =>
          Game(createdTime, hostId, gameId, otherPlayers, gameStatus)
      })
      .transact(Database.xa)
      .attemptT
  }

  private def getGameQuery(gameId: GameId) =
    sql"""
      SELECT created_time, host_id, other_players, game_status
      FROM games_view
      WHERE id=$gameId
      """
      .query[(Timestamp, UserId, Set[UserId], GameStatus)]
      .unique
      .map({
        case (createdTime, hostId, otherPlayers, gameStatus) =>
          Game(createdTime, hostId, gameId, otherPlayers, gameStatus)
      })

  def getGame(gameId: GameId): EitherT[IO, Throwable, Game] = {
    getGameQuery(gameId)
      .transact(Database.xa)
      .attemptT
  }

  def addPlayer(gameId: GameId, userId: UserId): EitherT[IO, Throwable, Game] = {
    val cmd = for {
      _    <- sql"INSERT INTO games_users (game_id, user_id) VALUES ($gameId, $userId)".update.run
      game <- getGameQuery(gameId)
    } yield game

    cmd.transact(Database.xa).attemptT
  }

  def removePlayer(gameId: GameId, userId: UserId): EitherT[IO, Throwable, Game] = {
    val cmd = for {
      _    <- sql"DELETE FROM games_users WHERE game_id=$gameId AND user_id=$userId".update.run
      game <- getGameQuery(gameId)
    } yield game

    cmd.transact(Database.xa).attemptT
  }

  def cancelGame(gameId: GameId, canceledBy: UserId): EitherT[IO, Throwable, Game] = {
    val cmd = for {
      _    <- sql"""
        UPDATE games
        SET game_status = ${Canceled: GameStatus}
        WHERE id=$gameId AND host_id=$canceledBy
        """.update.run
      game <- getGameQuery(gameId)
    } yield game

    cmd.transact(Database.xa).attemptT
  }

  def startGame(gameId: GameId, startedBy: UserId): EitherT[IO, Throwable, Game] = {
    val cmd = for {
      _    <- sql"""
        UPDATE games
        SET game_status = ${InProgress: GameStatus}
        WHERE id=$gameId AND host_id=$startedBy
        """.update.run
      game <- getGameQuery(gameId)
    } yield game

    cmd.transact(Database.xa).attemptT
  }
}
