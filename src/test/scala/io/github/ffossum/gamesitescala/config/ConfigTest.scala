package io.github.ffossum.gamesitescala.config

import org.scalatest.{FunSuite, Matchers}

class ConfigTest extends FunSuite with Matchers {

  test("valid config") {

    val config = Config.validateConfig(
      Map(
        ConfigKeys.DEEPSTREAM_HOST     -> "http://127.0.0.1:3030",
        ConfigKeys.DEEPSTREAM_USERNAME -> "admin",
        ConfigKeys.DEEPSTREAM_PASSWORD -> "admin",
        ConfigKeys.JWT_SECRET          -> "secret"
      )
    )

    config shouldBe a[Right[_, _]]
  }

  test("missing required variables") {
    Config.validateConfig(Map.empty) shouldBe a[Left[_, _]]
  }
}
