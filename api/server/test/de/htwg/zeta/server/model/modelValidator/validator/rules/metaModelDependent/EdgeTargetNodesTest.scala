package de.htwg.zeta.server.model.modelValidator.validator.rules.metaModelDependent

import scala.collection.immutable.Seq

import models.modelDefinitions.metaModel.elements.MAttribute
import models.modelDefinitions.metaModel.elements.MClass
import models.modelDefinitions.metaModel.elements.MLinkDef
import models.modelDefinitions.metaModel.elements.MReference
import models.modelDefinitions.model.elements.Edge
import models.modelDefinitions.model.elements.Node
import models.modelDefinitions.model.elements.ToNodes
import org.scalatest.FlatSpec
import org.scalatest.Matchers

class EdgeTargetNodesTest extends FlatSpec with Matchers {
  val mReference = MReference("edgeType", sourceDeletionDeletesTarget = false, targetDeletionDeletesSource = false, Seq[MLinkDef](), Seq[MLinkDef](), Seq[MAttribute]())
  val rule = new EdgeTargetNodes("edgeType", Seq("target1", "target2"))

  "isValid" should "return true on edges of type edgeType with valid target nodes" in {

    val target1 = MClass(
      name = "target1",
      abstractness = false,
      superTypes = Seq(),
      inputs = Seq(),
      outputs = Seq(),
      attributes = Seq()
    )

    val toNodes1 = ToNodes(`type` = target1, nodes = Seq(
      Node(
        id = "",
        `type` = target1,
        _outputs = Seq(),
        _inputs = Seq(),
        attributes = Seq()
      )
    ))

    val edge1 = Edge.apply2("", mReference, Seq(), Seq(toNodes1), Seq())

    rule.isValid(edge1).get should be(true)

    val target2 = MClass(
      name = "target2",
      abstractness = false,
      superTypes = Seq(),
      inputs = Seq(),
      outputs = Seq(),
      attributes = Seq()
    )

    val toNodes2 = ToNodes(`type` = target1, nodes = Seq(
      Node(
        id = "",
        `type` = target1,
        _outputs = Seq(),
        _inputs = Seq(),
        attributes = Seq()
      ),
      Node(
        id = "",
        `type` = target2,
        _outputs = Seq(),
        _inputs = Seq(),
        attributes = Seq()
      )
    ))

    val edge2 = Edge.apply2("", mReference, Seq(), Seq(toNodes2), Seq())

    rule.isValid(edge2).get should be(true)

  }

  it should "return false on edges of type edgeType with invalid target nodes" in {
    val invalidTarget = MClass(
      name = "invalidTarget",
      abstractness = false,
      superTypes = Seq(),
      inputs = Seq(),
      outputs = Seq(),
      attributes = Seq()
    )

    val invalidToNodes = ToNodes(`type` = invalidTarget, nodes = Seq(
      Node(
        id = "",
        `type` = invalidTarget,
        _outputs = Seq(),
        _inputs = Seq(),
        attributes = Seq()
      )
    ))

    val edge1 = Edge.apply2("", mReference, Seq(), Seq(invalidToNodes), Seq())

    rule.isValid(edge1).get should be(false)
  }

  it should "return None on non-matching edges" in {
    val differentMReference = MReference("differentEdgeType", sourceDeletionDeletesTarget = false, targetDeletionDeletesSource = false, Seq[MLinkDef](), Seq[MLinkDef](), Seq[MAttribute]())
    val edge = Edge.apply2("", differentMReference, Seq(), Seq(), Seq())
    rule.isValid(edge) should be (None)
  }

  "dslStatement" should "return the correct string" in {
    rule.dslStatement should be ("""Targets ofEdges "edgeType" areOfTypes Seq("target1", "target2")""")
  }
}