package de.htwg.zeta.parser

import de.htwg.zeta.server.generator.parser.CommonParserMethods
import org.scalatest.FreeSpec
import org.scalatest.Matchers

class UniteParsersTest extends FreeSpec with Matchers {

  "Multiple Parsers extending 'UniteParsers'" - {

    object NodeParser extends CommonParserMethods with UniteParsers{
      def parseNode = "node" ~ "{}"
    }
    object EdgeParser extends CommonParserMethods with UniteParsers{
      def parseEdge = "edge" ~ "{}"
    }

    object DiagramParser extends CommonParserMethods with UniteParsers {

      private def node = include(NodeParser.parseNode)
      private def edge = include(EdgeParser.parseEdge)

      def parseDiagram = "diagram" ~ "{"~ rep(node | edge) ~"}"
    }

    "will parse a simple example and unite the parsers" -{
      val dia =
        """|
           |diagram {
           |  node {}
           |  edge {}
           |  node {}
           |}""".stripMargin

      "by calling the unite method" in {
        DiagramParser.parse(DiagramParser.parseDiagram, dia).successful shouldBe true
      }
    }
  }

}
