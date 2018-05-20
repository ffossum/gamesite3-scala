package io.github.ffossum.gamesitescala.util

import io.circe.parser
import io.circe.syntax._
import org.scalatest.prop.Checkers
import org.scalatest.{FunSuite, Matchers}

class GsonSyntaxTest extends FunSuite with Matchers with Checkers {
  import GsonSyntax._

  test("Int as gson") {
    check { intValue: Int =>
      val gson = intValue.asGson
      val json = intValue.asJson

      gson.toString === json.toString
    }
  }

  test("Double as gson") {
    check { doubleValue: Int =>
      val gson = doubleValue.asGson
      val json = doubleValue.asJson

      gson.toString === json.toString
    }
  }

  test("BigDecimal as gson") {
    check { value: BigDecimal =>
      val gson = value.asGson
      val json = value.asJson

      gson.toString === json.toString
    }
  }

  test("gson to circe") {
    val json  = parser.parse("""{"a":[10,3.14,3.00e8,null]}""").right.get
    val gson  = circeToGson(json)
    val json2 = gsonToCirce(gson)

    json.noSpaces shouldBe """{"a":[10,3.14,3.00e8,null]}"""
  }

}
