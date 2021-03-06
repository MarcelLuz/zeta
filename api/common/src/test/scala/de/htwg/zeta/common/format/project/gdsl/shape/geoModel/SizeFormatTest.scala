package de.htwg.zeta.common.format.project.gdsl.shape.geoModel

import de.htwg.zeta.common.models.project.gdsl.shape.geomodel.Size
import org.scalatest.FreeSpec
import org.scalatest.Matchers
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json

//noinspection ScalaStyle
class SizeFormatTest extends FreeSpec with Matchers {

  "A SizeFormat should" - {
    "write an object" in {
      val result = SizeFormat().writes(Size(10, 12))
      result.toString() shouldBe
        """{"width":10,"height":12}"""
    }
    "read an object" in {
      val result = SizeFormat().reads(Json.parse(
        """{"width":10,"height":12}"""
      ))
      result shouldBe JsSuccess(Size(10, 12))
    }
    "fail in reading an invalid input" in {
      val result = SizeFormat().reads(Json.parse(
        """{"invalid":{"r":23}}"""
      ))
      result.isSuccess shouldBe false
    }
  }

}
