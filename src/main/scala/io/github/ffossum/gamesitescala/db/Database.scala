package io.github.ffossum.gamesitescala.db
import cats.effect._
import doobie.util.transactor.Transactor

object Database {
  val xa: Transactor[IO] = doobie.Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://172.17.0.2:5432/postgres",
    "postgres",
    ""
  )
}
