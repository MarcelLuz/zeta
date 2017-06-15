package de.htwg.zeta.server.model.modelValidator.validator.rules.metaModelDependent

import scala.collection.immutable.Seq

import models.modelDefinitions.metaModel.elements.AttributeType.StringType
import models.modelDefinitions.metaModel.elements.AttributeValue.MString
import models.modelDefinitions.metaModel.elements.MAttribute
import models.modelDefinitions.metaModel.elements.MReference
import models.modelDefinitions.model.elements.Edge
import org.scalatest.FlatSpec
import org.scalatest.Matchers

class EdgeAttributesLocalUniqueTest extends FlatSpec with Matchers {

  val rule = new EdgeAttributesLocalUnique("edgeType", "attributeType")
  val mReference = MReference(
    "edgeType",
    sourceDeletionDeletesTarget = false,
    targetDeletionDeletesSource = false,
    Seq.empty,
    Seq.empty,
    Seq[MAttribute]()
  )

  "isValid" should "return true on valid edges" in {
    val attribute = Map("attributeType" -> Seq(MString("valueOne"), MString("valueTwo"), MString("valueThree")))
    val edge = Edge("edgeOneId", mReference, Seq(), Seq(), attribute)

    rule.isValid(edge).get should be(true)
  }

  it should "return false on invalid edges" in {
    val attribute = Map("attributeType" -> Seq(MString("dupValue"), MString("dupValue"), MString("valueThree")))
    val edge = Edge("edgeOneId", mReference, Seq(), Seq(), attribute)

    rule.isValid(edge).get should be(false)
  }

  it should "return None on non-matching edges" in {
    val mReference = MReference(
      "differentEdgeType",
      sourceDeletionDeletesTarget = false,
      targetDeletionDeletesSource = false,
      Seq.empty,
      Seq.empty,
      Seq[MAttribute]()
    )
    val edge = Edge("edgeOneId", mReference, Seq(), Seq(), Map.empty)

    rule.isValid(edge) should be(None)
  }

  "generateFor" should "generate this rule from the meta model" in {
    val localUniqueAttribute = MAttribute(
      "attributeName",
      globalUnique = false,
      localUnique = true,
      StringType,
      MString(""),
      constant = false,
      singleAssignment = false,
      "",
      ordered = false,
      transient = false,
      -1,
      0
    )
    val nonLocalUniqueAttribute = MAttribute(
      "attributeName2",
      globalUnique = false,
      localUnique = false,
      StringType,
      MString(""),
      constant = false,
      singleAssignment = false,
      "",
      ordered = false,
      transient = false,
      -1,
      0
    )
    val reference = MReference(
      "reference",
      sourceDeletionDeletesTarget = false,
      targetDeletionDeletesSource = false,
      Seq.empty,
      Seq.empty,
      Seq[MAttribute](localUniqueAttribute, nonLocalUniqueAttribute)
    )
    val metaModel = TestUtil.referencesToMetaModel(Seq(reference))
    val result = EdgeAttributesLocalUnique.generateFor(metaModel)

    result.size should be(1)
    result.head match {
      case rule: EdgeAttributesLocalUnique =>
        rule.edgeType should be("reference")
        rule.attributeType should be("attributeName")
      case _ => fail
    }
  }

}
