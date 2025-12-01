package fr.uga.miage.m1.app.mapper

import fr.uga.miage.m1.domain.model.Piste
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteEtatRequest
import fr.uga.miage.m1.responses.PisteResponse

object PisteMapper {

    fun toDomain(req: CreatePisteRequest): Piste =
        Piste(
            id = null,
            identifiant = req.identifiant,
            longueurM = req.longueurM,
            etat = req.etat ?: PisteEtat.LIBRE
        )

    fun toUpdatedEtat(existing: Piste, req: UpdatePisteEtatRequest): Piste =
        existing.copy(etat = req.etat)

    fun toResponse(piste: Piste): PisteResponse =
        PisteResponse(
            id = piste.id,
            identifiant = piste.identifiant,
            longueurM = piste.longueurM,
            etat = piste.etat
        )
}
