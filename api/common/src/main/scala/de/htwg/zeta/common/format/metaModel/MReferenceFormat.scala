package de.htwg.zeta.common.format.metaModel

import de.htwg.zeta.common.models.modelDefinitions.metaModel.elements.MReference
import play.api.libs.json.JsObject
import play.api.libs.json.JsResult
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.OFormat
import play.api.libs.json.Reads
import play.api.libs.json.Writes

object MReferenceFormat extends OFormat[MReference] {

  val sName = "name"
  val sDescription = "description"
  val sSourceDeletionDeletesTarget = "sourceDeletionDeletesTarget"
  val sTargetDeletionDeletesSource = "targetDeletionDeletesSource"
  val sSourceClassName = "sourceClassName"
  val sTargetClassName = "targetClassName"
  val sAttributes = "attributes"
  val sMethods = "methods"

  override def writes(reference: MReference): JsObject = Json.obj(
    sName -> reference.name,
    sDescription -> reference.description,
    sSourceDeletionDeletesTarget -> reference.sourceDeletionDeletesTarget,
    sTargetDeletionDeletesSource -> reference.targetDeletionDeletesSource,
    sSourceClassName -> reference.sourceClassName,
    sTargetClassName -> reference.targetClassName,
    sAttributes -> Writes.seq(MAttributeFormat).writes(reference.attributes),
    sMethods -> Writes.seq(MethodFormat).writes(reference.methods)
  )

  override def reads(json: JsValue): JsResult[MReference] = {
    for {
      name <- (json \ sName).validate[String]
      description <- (json \ sDescription).validate[String]
      sourceDeletionDeletesTarget <- (json \ sSourceDeletionDeletesTarget).validate[Boolean]
      targetDeletionDeletesSource <- (json \ sTargetDeletionDeletesSource).validate[Boolean]
      sourceClassName <- (json \ sSourceClassName).validate[String]
      targetClassName <- (json \ sTargetClassName).validate[String]
      attributes <- (json \ sAttributes).validate(Reads.list(MAttributeFormat))
      methods <- (json \ sMethods).validate(Reads.list(MethodFormat))
    } yield {
      MReference(
        name = name,
        description = description,
        sourceDeletionDeletesTarget = sourceDeletionDeletesTarget,
        targetDeletionDeletesSource = targetDeletionDeletesSource,
        sourceClassName = sourceClassName,
        targetClassName = targetClassName,
        attributes = attributes,
        methods = methods
      )
    }
  }

}
