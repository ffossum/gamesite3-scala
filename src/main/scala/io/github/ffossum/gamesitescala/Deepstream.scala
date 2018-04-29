package io.github.ffossum.gamesitescala
import cats.effect.{Effect, IO}
import com.google.gson.JsonObject
import fs2.Stream
import fs2.async
import io.deepstream._

import scala.language.higherKinds

sealed trait DeepstreamEvent

object Deepstream {
  private val DEEPSTREAM_USERNAME = "secret server username"
  private val DEEPSTREAM_PASSWORD = "secret deepstream password"

  val client = new DeepstreamClient("localhost:6020")
  val credentials: JsonObject = {
    val obj = new JsonObject()
    obj.addProperty("username", DEEPSTREAM_USERNAME)
    obj.addProperty("password", DEEPSTREAM_PASSWORD)
    obj
  }

}
