package de.htwg.zeta.common.models.modelDefinitions.metaModel.elements

import scala.annotation.tailrec
import scala.collection.immutable.Seq

import de.htwg.zeta.common.models.modelDefinitions.metaModel.MetaModel.MetaModelTraverseWrapper
import de.htwg.zeta.common.models.modelDefinitions.metaModel.elements.AttributeType.MEnum
import play.api.libs.json.Json
import play.api.libs.json.JsResult
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.libs.json.Writes


/** The MClass implementation
 *
 * @param name           the name of the MClass instance
 * @param abstractness   defines if the MClass is abstract
 * @param superTypeNames the names of the supertypes of the MClass
 * @param inputs         the incoming MReferences
 * @param outputs        the outgoing MReferences
 * @param attributes     the attributes of the MClass
 */
case class MClass(
    name: String,
    description: String,
    abstractness: Boolean,
    superTypeNames: Seq[String],
    inputs: Seq[MReferenceLinkDef],
    outputs: Seq[MReferenceLinkDef],
    attributes: Seq[MAttribute],
    methods: Seq[Method]
) extends MObject {

  /** Attributes mapped to their own names. */
  val attributeMap: Map[String, MAttribute] = attributes.map(attribute => (attribute.name, attribute)).toMap

  /** Methods mapped to their own names. */
  val methodMap: Map[String, Method] = methods.map(method => (method.name, method)).toMap

}

object MClass {

  case class MClassTraverseWrapper(value: MClass, metaModel: MetaModelTraverseWrapper) {
    def superTypes: Seq[MClassTraverseWrapper] = {
      value.superTypeNames.map(name =>
        MClassTraverseWrapper(metaModel.classes(name).value, metaModel)
      )
    }

    /**
     * represents the supertype hierarchy of this particular MClass
     */
    lazy val typeHierarchy: Seq[MClassTraverseWrapper] = getSuperHierarchy(Seq(this), superTypes)

    /**
     * Determines the supertype hierarchy of this particular MClass
     *
     * @param acc     accumulated value of recursion
     * @param inspect the next MClass to check
     * @return MClasses that take part in the supertype hierarchy
     */
    private def getSuperHierarchy(acc: Seq[MClassTraverseWrapper], inspect: Seq[MClassTraverseWrapper]): Seq[MClassTraverseWrapper] = {
      inspect.foldLeft(acc) { (a, m) =>
        if (a.exists(_.value.name == m.value.name)) {
          a
        } else {
          getSuperHierarchy(acc :+ m, m.superTypes)
        }
      }
    }

    /**
     * Checks if certain input relationship is allowed, also based on supertypes
     *
     * @param inputName the name of the incoming relationship
     * @return true if the relationship is defined within the type hierarchy
     */
    def typeHasInput(inputName: String): Boolean = {
      typeHierarchy.exists(
        cls => cls.value.inputs.exists(link => link.referenceName == inputName)
      )
    }

    /**
     * Checks if certain output relationship is allowed, also based on supertypes
     *
     * @param outputName the name of the outgoing relationship
     * @return true if the relationship is defined within the type hierarchy
     */
    def typeHasOutput(outputName: String): Boolean = {
      typeHierarchy.exists(
        cls => cls.value.outputs.exists(link => link.referenceName == outputName)
      )
    }

    /**
     * Checks if MClass has a certain supertype
     *
     * @param superName the name of the supertype in question
     * @return true if the given name belongs to a supertype
     */
    def typeHasSuperType(superName: String): Boolean = {
      typeHierarchy.exists(
        cls => cls.value.name == superName
      )
    }

    /**
     * Returns all effective (inherited) MAttributes of this MClass
     *
     * @return the MAttributes
     */
    def getTypeMAttributes: Seq[MAttribute] = {
      typeHierarchy.flatMap(_.value.attributes)
    }

    /**
     * Finds an MAttribute within supertypes
     *
     * @param attributeName the name of the attribute to find
     * @return the MAttribute, if present
     */
    def findMAttribute(attributeName: String): Option[MAttribute] = {
      @tailrec
      def find(remaining: Seq[MClass]): Option[MAttribute] = {
        if (remaining.isEmpty) {
          None
        } else {
          val head = remaining.head
          val attribute = head.attributes.find(_.name == attributeName)
          if (attribute.isDefined) attribute else find(remaining.filter(_ != head))
        }
      }

      find(typeHierarchy.map(_.value))
    }

  }

  def playJsonReads(enums: Seq[MEnum]): Reads[MClass] = {
    new Reads[MClass] {
      override def reads(json: JsValue): JsResult[MClass] = {
        for {
          name <- (json \ "name").validate[String]
          description <- (json \ "description").validate[String]
          abstractness <- (json \ "abstractness").validate[Boolean]
          superTypeNames <- (json \ "superTypeNames").validate(Reads.list[String])
          inputs <- (json \ "inputs").validate(Reads.list[MReferenceLinkDef])
          outputs <- (json \ "outputs").validate(Reads.list[MReferenceLinkDef])
          attributes <- (json \ "attributes").validate(Reads.list(MAttribute.playJsonReads(enums)))
          methods <- (json \ "methods").validate(Reads.list(Method.playJsonReads(enums)))
        } yield {
          MClass(
            name = name,
            description = description,
            abstractness = abstractness,
            superTypeNames = superTypeNames,
            inputs = inputs,
            outputs = outputs,
            attributes = attributes,
            methods = methods
          )
        }
      }
    }
  }

  implicit val playJsonWrites: Writes[MClass] = Json.writes[MClass]

}
