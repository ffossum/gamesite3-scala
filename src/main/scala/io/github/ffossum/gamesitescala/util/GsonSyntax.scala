package io.github.ffossum.gamesitescala.util

import com.google.gson.{JsonParser => GsonParser, JsonElement => GsonElement}
import io.circe._
import io.circe.syntax._

object GsonSyntax {
  private lazy val gsonParser = new GsonParser()

  implicit class CirceToGson[T: Encoder](t: T) {
    def asGson: GsonElement = gsonParser.parse(t.asJson.noSpaces)
  }
}
