package io.github.ffossum.gamesitescala.user

import org.scalatest.{FunSuite, Matchers}
import io.circe.syntax._

class PrivateUserDataTest extends FunSuite with Matchers {
  test("user id is encoded as json string") {
    val userData = PrivateUserData(
      UserId(42),
      Email("test@test.com"),
      Username("MrTest")
    )

    val json = userData.asJson

    json.spaces2 shouldBe
      """{
        |  "id" : "42",
        |  "email" : "test@test.com",
        |  "username" : "MrTest"
        |}""".stripMargin
  }
}
