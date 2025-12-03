package fr.uga.miage.m1.app.mapper

import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.requests.remote.SharedVolRequest
import fr.uga.miage.m1.responses.remote.SharedVolResponse

object SharedVolInboundMapper {

    fun toDomain(req: SharedVolRequest): Pair<Avion, Vol> =
        map(
            SharedVolRequest(
                numeroVol = req.numeroVol,
                origine = req.origine,
                destination = req.destination,
                heureDepart = req.heureDepart,
                heureArrivee = req.heureArrivee,
                etat = req.etat,
                avionImmatriculation = req.avionImmatriculation,
                avionType = req.avionType,
                avionCapacite = req.avionCapacite,
                avionEtat = req.avionEtat
            )
        )

    fun fromResponse(res: SharedVolResponse): Pair<Avion, Vol> =
        map(
            SharedVolRequest(
                numeroVol = res.numeroVol,
                origine = res.origine,
                destination = res.destination,
                heureDepart = res.heureDepart,
                heureArrivee = res.heureArrivee,
                etat = res.etat,
                avionImmatriculation = res.avionImmatriculation,
                avionType = res.avionType,
                avionCapacite = res.avionCapacite,
                avionEtat = res.avionEtat
            )
        )

    private fun map(inb: SharedVolRequest): Pair<Avion, Vol> {
        val avion = Avion(
            id = null,
            immatriculation = inb.avionImmatriculation,
            type = inb.avionType,
            capacite = inb.avionCapacite,
            etat = inb.avionEtat,
            hangarId = null
        )

        val vol = Vol(
            id = null,
            numeroVol = inb.numeroVol,
            origine = inb.origine,
            destination = inb.destination,
            heureDepart = inb.heureDepart,
            heureArrivee = inb.heureArrivee,
            etat = inb.etat,
            avionId = null,
            pisteId = null
        )

        return avion to vol
    }
}
