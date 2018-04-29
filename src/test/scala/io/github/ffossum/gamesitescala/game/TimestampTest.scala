package io.github.ffossum.gamesitescala.game

import java.time.Instant

import cats.effect.IO
import org.scalatest.{FunSuite, Matchers}

import io.circe.syntax._
import scala.util.Success

class TimestampTest extends FunSuite with Matchers {
  test("now returns a valid timestamp") {
    val timestamp = Timestamp.now[IO].unsafeRunSync
    timestamp.value shouldBe a[Instant]
  }
  test("parse an ISO string") {
    Timestamp.parse("2018-04-29T09:09:34.920Z") shouldBe a[Success[Timestamp]]
  }
  test("json encode/decode") {
    val timestamp = Timestamp.now[IO].unsafeRunSync
    val json      = timestamp.asJson

    json.as[Timestamp] shouldBe Right(timestamp)
  }
}
