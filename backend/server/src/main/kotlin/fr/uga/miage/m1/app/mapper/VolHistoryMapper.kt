package fr.uga.miage.m1.app.mapper

import fr.uga.miage.m1.domain.model.VolHistory
import fr.uga.miage.m1.persistence.entity.VolHistoryEntity
import fr.uga.miage.m1.responses.VolHistoryResponse

object VolHistoryMapper {

    fun toEntity(model: VolHistory) =
        VolHistoryEntity(
            id = model.id,
            volId = model.volId,
            etat = model.etat,
            changedAt = model.changedAt
        )

    fun toDomain(entity: VolHistoryEntity) =
        VolHistory(
            id = entity.id,
            volId = entity.volId,
            etat = entity.etat,
            changedAt = entity.changedAt
        )

    fun toResponse(model: VolHistory) =
        VolHistoryResponse(
            id = model.id,
            etat = model.etat,
            changedAt = model.changedAt
        )
}
