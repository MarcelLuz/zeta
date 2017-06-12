package de.htwg.zeta.server.model.modelValidator.validator.rules.validatorDsl

import de.htwg.zeta.server.model.modelValidator.validator.rules.metaModelDependent.EdgeAttributeEnumTypes
import de.htwg.zeta.server.model.modelValidator.validator.rules.metaModelDependent.EdgeAttributeScalarTypes
import de.htwg.zeta.server.model.modelValidator.validator.rules.metaModelDependent.EdgeAttributesGlobalUnique
import de.htwg.zeta.server.model.modelValidator.validator.rules.metaModelDependent.EdgeAttributesLocalUnique
import de.htwg.zeta.server.model.modelValidator.validator.rules.metaModelDependent.EdgeAttributesLowerBound
import de.htwg.zeta.server.model.modelValidator.validator.rules.metaModelDependent.EdgeAttributesUpperBound
import models.modelDefinitions.metaModel.elements.AttributeType.BoolType
import models.modelDefinitions.metaModel.elements.AttributeType.DoubleType
import models.modelDefinitions.metaModel.elements.AttributeType.IntType
import models.modelDefinitions.metaModel.elements.AttributeType.StringType

class AttributesOfTypeInEdges(attributeType: String, edgeType: String) {

  def haveUpperBound(upperBound: Int): EdgeAttributesUpperBound = new EdgeAttributesUpperBound(edgeType, attributeType, upperBound)

  def haveLowerBound(lowerBound: Int): EdgeAttributesLowerBound = new EdgeAttributesLowerBound(edgeType, attributeType, lowerBound)

  def areLocalUnique(): EdgeAttributesLocalUnique = new EdgeAttributesLocalUnique(edgeType, attributeType)

  def areGlobalUnique(): EdgeAttributesGlobalUnique = new EdgeAttributesGlobalUnique(edgeType, attributeType)

  def areOfScalarType(scalarType: String): EdgeAttributeScalarTypes = scalarType match {
    case "String" => new EdgeAttributeScalarTypes(edgeType, attributeType, StringType)
    case "Int" => new EdgeAttributeScalarTypes(edgeType, attributeType, IntType)
    case "Bool" => new EdgeAttributeScalarTypes(edgeType, attributeType, BoolType)
    case "Double" => new EdgeAttributeScalarTypes(edgeType, attributeType, DoubleType)
  }

  def areOfEnumType(enumType: String): EdgeAttributeEnumTypes = new EdgeAttributeEnumTypes(edgeType, attributeType, enumType)

}
