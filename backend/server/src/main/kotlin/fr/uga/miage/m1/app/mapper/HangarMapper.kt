package fr.uga.miage.m1.app.mapper

import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.requests.CreateHangarRequest
import fr.uga.miage.m1.requests.UpdateHangarRequest
import fr.uga.miage.m1.responses.HangarResponse

object HangarMapper {

    fun toDomain(req: CreateHangarRequest): Hangar =
        Hangar(
            id = null,
            identifiant = req.identifiant,
            capacite = req.capacite,
            etat = req.etat
        )

    fun toUpdatedDomain(current: Hangar, req: UpdateHangarRequest): Hangar =
        Hangar(
            id = current.id,
            identifiant = current.identifiant, // identifiant never changes
            capacite = req.capacite ?: current.capacite,
            etat = req.etat ?: current.etat
        )

    fun toResponse(hangar: Hangar): HangarResponse =
        HangarResponse(
            id = hangar.id,
            identifiant = hangar.identifiant,
            capacite = hangar.capacite,
            etat = hangar.etat
        )
}
