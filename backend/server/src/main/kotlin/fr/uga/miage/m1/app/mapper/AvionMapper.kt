package fr.uga.miage.m1.app.mapper

import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.requests.CreateAvionRequest
import fr.uga.miage.m1.requests.UpdateAvionRequest
import fr.uga.miage.m1.responses.AvionResponse

object AvionMapper {
    fun toDomain(req: CreateAvionRequest): Avion =
        Avion(
            id = null,
            immatriculation = req.immatriculation.uppercase(),
            type = req.type,
            capacite = req.capacite,
            etat = req.etat,
            hangarId = req.hangarId
        )

    fun toUpdatedDomain(current: Avion, req: UpdateAvionRequest): Avion =
        Avion(
            id = current.id,
            immatriculation = current.immatriculation,
            type = req.type ?: current.type,
            capacite = req.capacite ?: current.capacite,
            etat = req.etat ?: current.etat,
            hangarId = req.hangarId ?: current.hangarId
        )

    fun toResponse(avion: Avion): AvionResponse =
        AvionResponse(
            id = avion.id,
            immatriculation = avion.immatriculation,
            type = avion.type,
            capacite = avion.capacite,
            etat = avion.etat,
            hangarId = avion.hangarId
        )
}
