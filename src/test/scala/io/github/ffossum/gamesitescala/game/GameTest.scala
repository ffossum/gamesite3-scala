package io.github.ffossum.gamesitescala.game

import io.circe.syntax._
import io.github.ffossum.gamesitescala.user.UserId
import org.scalatest.{FunSuite, Matchers}

class GameTest extends FunSuite with Matchers {
  test("json encode/decode") {
    val game = Game(
      Timestamp.parse("2018-04-29T13:33:50.949Z").get,
      UserId(1),
      GameId(1),
      Set(UserId(2), UserId(3)),
      GameStatus.NotStarted
    )

    val json = game.asJson
    json.spaces2 shouldBe
      """{
        |  "createdTime" : "2018-04-29T13:33:50.949Z",
        |  "host" : "1",
        |  "id" : "1",
        |  "players" : [
        |    "2",
        |    "3"
        |  ],
        |  "status" : "not_started"
        |}""".stripMargin

    json.as[Game] shouldBe Right(game)
  }
}
