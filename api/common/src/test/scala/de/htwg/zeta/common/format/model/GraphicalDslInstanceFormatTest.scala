package de.htwg.zeta.common.format.model

import java.util.UUID

import de.htwg.zeta.common.format.project.AttributeFormat
import de.htwg.zeta.common.format.project.AttributeTypeFormat
import de.htwg.zeta.common.format.project.AttributeValueFormat
import de.htwg.zeta.common.format.project.MethodFormat
import org.scalatest.FreeSpec
import org.scalatest.Matchers
import play.api.libs.json.Json

class GraphicalDslInstanceFormatTest extends FreeSpec with Matchers {
  val attributeTypeFormat = new AttributeTypeFormat()
  val attributeValueFormat = new AttributeValueFormat()
  val attributeFormat = new AttributeFormat(attributeTypeFormat, attributeValueFormat)
  val methodFormat = new MethodFormat(attributeTypeFormat)
  val nodeFormat = new NodeFormat(attributeFormat, attributeValueFormat, methodFormat)
  val edgeFormat = new EdgeFormat(attributeFormat, attributeValueFormat, methodFormat)
  val format = new GraphicalDslInstanceFormat(nodeFormat, edgeFormat,attributeFormat, attributeValueFormat, methodFormat)

  "Parse input" - {
    "should work" in {
      val inp =
        """
         |{
         |	"name": "1",
         |  "id": "00000000-0000-0000-0000-0000f00b1a0c",
         |	"graphicalDslId": "543efc93-6501-4bcf-8fa1-15ea2444fc53",
         |	"nodes": [{
         |			"name": "cd4b689a-b166-40a3-b617-8ca49296e5b7",
         |			"className": "Entity",
         |			"outputEdgeNames": [],
         |			"inputEdgeNames": [],
         |			"attributes": [{
         |					"name": "EAttr",
         |					"globalUnique": false,
         |					"localUnique": false,
         |					"type": "string",
         |					"default": {
         |						"type": "string",
         |						"value": ""
         |					},
         |					"constant": false,
         |					"singleAssignment": false,
         |					"expression": "",
         |					"ordered": false,
         |					"transient": false,
         |					"id": "00000000-0000-0000-0000-0000f00b1a0b"
         |				}
         |			],
         |			"attributeValues": {
         |				"EAttr":  [{
         |					"value": "...",
         |					"type": "string"
         |				}]
         |			},
         |			"methods": []
         |		}
         |	],
         |	"edges": [],
         |	"attributes": [],
         |	"attributeValues": {},
         |	"methods": [],
         |  "uiState": ""
         |}
        """.stripMargin

      val result = Json.fromJson(Json.parse(inp))(format)

      if(result.isError) println(result)

      result.isSuccess shouldBe true
    }
  }

}
