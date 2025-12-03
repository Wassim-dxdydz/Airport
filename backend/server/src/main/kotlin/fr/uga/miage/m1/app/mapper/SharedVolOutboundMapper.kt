package fr.uga.miage.m1.app.mapper

import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.requests.remote.SharedVolRequest
import fr.uga.miage.m1.responses.remote.SharedVolResponse

object SharedVolOutboundMapper {

    fun toResponse(vol: Vol, avion: Avion): SharedVolResponse =
        SharedVolResponse(
            numeroVol = vol.numeroVol,
            origine = vol.origine,
            destination = vol.destination,
            heureDepart = vol.heureDepart,
            heureArrivee = vol.heureArrivee,
            etat = vol.etat,

            avionImmatriculation = avion.immatriculation,
            avionType = avion.type,
            avionCapacite = avion.capacite,
            avionEtat = avion.etat
        )

    fun toRequest(vol: Vol, avion: Avion): SharedVolRequest =
        SharedVolRequest(
            numeroVol = vol.numeroVol,
            origine = vol.origine,
            destination = vol.destination,
            heureDepart = vol.heureDepart,
            heureArrivee = vol.heureArrivee,
            etat = vol.etat,

            avionImmatriculation = avion.immatriculation,
            avionType = avion.type,
            avionCapacite = avion.capacite,
            avionEtat = avion.etat
        )

}
