package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.requests.UpdateVolRequest
import fr.uga.miage.m1.responses.VolResponse

object VolMapper {

    fun toDomain(req: CreateVolRequest): Vol =
        Vol(
            id = null,
            numeroVol = req.numeroVol,
            origine = req.origine,
            destination = req.destination,
            heureDepart = req.heureDepart,
            heureArrivee = req.heureArrivee,
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null,
        )

    fun toUpdatedDomain(current: Vol, req: UpdateVolRequest): Vol =
        Vol(
            id = current.id,
            numeroVol = current.numeroVol, // immutable
            origine = req.origine ?: current.origine,
            destination = req.destination ?: current.destination,
            heureDepart = req.heureDepart ?: current.heureDepart,
            heureArrivee = req.heureArrivee ?: current.heureArrivee,
            etat = req.etat ?: current.etat,
            avionId = req.avionId ?: current.avionId,
            pisteId = current.pisteId,
        )

    fun toResponse(vol: Vol): VolResponse =
        VolResponse(
            id = vol.id,
            numeroVol = vol.numeroVol,
            origine = vol.origine,
            destination = vol.destination,
            heureDepart = vol.heureDepart,
            heureArrivee = vol.heureArrivee,
            etat = vol.etat,
            avionId = vol.avionId,
            pisteId = vol.pisteId,
            createdAt = null,   // domain does not know timestamps (Option A)
            updatedAt = null
        )
}
