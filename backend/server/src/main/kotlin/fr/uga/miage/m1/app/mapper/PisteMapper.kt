package fr.uga.miage.m1.app.mapper

import fr.uga.miage.m1.domain.model.Piste
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteRequest
import fr.uga.miage.m1.responses.PisteResponse

object PisteMapper {

    fun toDomain(req: CreatePisteRequest): Piste =
        Piste(
            id = null,
            identifiant = req.identifiant,
            longueurM = req.longueurM,
            etat = PisteEtat.LIBRE
        )

    fun toUpdatedDomain(current: Piste, req: UpdatePisteRequest): Piste =
        Piste(
            id = current.id,
            identifiant = req.identifiant ?: current.identifiant,
            longueurM = req.longueurM ?: current.longueurM,
            etat = req.etat ?: current.etat
        )

    fun toResponse(piste: Piste): PisteResponse =
        PisteResponse(
            id = piste.id,
            identifiant = piste.identifiant,
            longueurM = piste.longueurM,
            etat = piste.etat
        )
}
