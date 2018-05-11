package de.htwg.zeta.common.models.project.gdsl.shape.geomodel

import de.htwg.zeta.common.models.project.gdsl.style.Style

case class RepeatingBox(
    editable: Boolean,
    position: Position,
    size: Size,
    forEach: String, // TODO?
    forAs: String, // TODO?
    childGeoModels: List[GeoModel],
    style: Style
) extends GeoModel
