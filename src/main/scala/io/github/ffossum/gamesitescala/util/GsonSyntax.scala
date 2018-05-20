package io.github.ffossum.gamesitescala.util

import com.google.{gson => G}
import io.circe._
import io.circe.syntax._

object GsonSyntax {
  private lazy val gsonParser = new G.JsonParser()

  implicit class CirceToGson[T: Encoder](t: T) {
    def asGson: G.JsonElement = circeToGson(t)
  }

  implicit class GsonToCirce(gson: G.JsonElement) {
    def as[D: Decoder]: Decoder.Result[D] = {
      gsonToCirce(gson).as[D]
    }
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

  def gsonToCirce(gson: G.JsonElement): Json = {
    import scala.collection.JavaConverters._

    if (gson.isJsonArray) {
      val array  = gson.getAsJsonArray
      val values = array.iterator().asScala.toList.map(gsonToCirce)
      Json.arr(values: _*)
    } else if (gson.isJsonObject) {
      val obj    = gson.getAsJsonObject
      val fields = obj.entrySet.asScala.toList.map(x => (x.getKey, gsonToCirce(x.getValue)))
      Json.obj(fields: _*)

    } else if (gson.isJsonPrimitive) {
      val primitive = gson.getAsJsonPrimitive
      if (primitive.isBoolean) {
        Json.fromBoolean(primitive.getAsBoolean)
      } else if (primitive.isString) {
        Json.fromString(primitive.getAsString)
      } else {
        parser.parse(primitive.toString).right.get
      }

    } else {
      Json.Null
    }
  }

  def circeToGson[T: Encoder](t: T): G.JsonElement = circeToGson(t.asJson)
}
