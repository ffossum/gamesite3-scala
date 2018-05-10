package io.github.ffossum.gamesitescala.util

import com.google.{gson => G}
import io.circe._
import io.circe.syntax._

object GsonSyntax {
  private lazy val gsonParser = new G.JsonParser()

  implicit class CirceToGson[T: Encoder](t: T) {
    def asGson: G.JsonElement = circeToGson(t)
  }

  def circeToGson(json: Json): G.JsonElement = {
    json.fold[G.JsonElement](
      G.JsonNull.INSTANCE,
      (boolean: Boolean) => new G.JsonPrimitive(boolean),
      (number: JsonNumber) => {
        gsonParser.parse(number.toString)
      },
      (string: String) => new G.JsonPrimitive(string),
      (jsonArray: Vector[Json]) => {
        val result = new G.JsonArray
        jsonArray.foreach(x => result.add(circeToGson(x)))
        result
      },
      (jsonObject: JsonObject) => {
        val result = new G.JsonObject
        jsonObject.toIterable.foreach {
          case (key, value) => result.add(key, circeToGson(value))
        }
        result
      }
    )
  }
  def circeToGson[T: Encoder](t: T): G.JsonElement = circeToGson(t.asJson)
}
