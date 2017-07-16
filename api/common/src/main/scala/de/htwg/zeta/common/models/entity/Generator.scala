package de.htwg.zeta.common.models.entity

import java.util.UUID

case class Generator(
    id: UUID,
    name: String,
    imageId: UUID,
    files: Map[UUID, String] = Map(),
    deleted: Option[Boolean] = Some(false)
) extends Entity
