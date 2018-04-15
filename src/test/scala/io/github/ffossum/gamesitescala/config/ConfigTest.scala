package io.github.ffossum.gamesitescala.config

import org.scalatest.{FunSuite, Matchers}

class ConfigTest extends FunSuite with Matchers {

  test("valid config") {

    val config = Config.validateConfig(
      Map("DEEPSTREAM_HOST"     -> "http://127.0.0.1:3030",
          "DEEPSTREAM_USERNAME" -> "admin",
          "DEEPSTREAM_PASSWORD" -> "admin",
          "JWT_SECRET"          -> "secret"))

    config shouldBe a[Right[_, _]]
  }

  test("missing required variables") {
    Config.validateConfig(Map.empty) shouldBe a[Left[_, _]]
  }
}
