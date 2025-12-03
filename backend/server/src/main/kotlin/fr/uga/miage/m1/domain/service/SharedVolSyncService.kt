package fr.uga.miage.m1.domain.service

import fr.uga.miage.m1.app.mapper.SharedVolInboundMapper
import fr.uga.miage.m1.app.mapper.SharedVolOutboundMapper
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.RemoteAirportPort
import fr.uga.miage.m1.domain.port.VolDataPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class SharedVolSyncService(
    private val avionPort: AvionDataPort,
    private val volPort: VolDataPort,
    private val remotePort: RemoteAirportPort
) {
    private val myAirportCode = "ATL"
    private val partnerAirportCode = "CDG"

    // Importe un vol reçu d'un partenaire
    fun import(avion: Avion, vol: Vol): Mono<Vol> {

        val avionMono = avionPort.findByImmatriculation(avion.immatriculation)
            .switchIfEmpty(avionPort.save(avion))   // create if missing

        return avionMono.flatMap { persistedAvion ->

            val volToStore = vol.copy(avionId = persistedAvion.id)

            volPort.findByNumeroVol(vol.numeroVol)
                .flatMap { existing ->
                    val merged = existing.copy(
                        origine = volToStore.origine,
                        destination = volToStore.destination,
                        heureDepart = volToStore.heureDepart,
                        heureArrivee = volToStore.heureArrivee,
                        etat = volToStore.etat,
                        avionId = persistedAvion.id
                    )
                    volPort.save(merged)
                }
                .switchIfEmpty(volPort.save(volToStore))
        }
    }

    // Exporte les vols à destination du partenaire
    fun exportForPartner(): Flux<Pair<Vol, Avion>> =
        volPort.findByDestination(partnerAirportCode)
            .flatMap { vol ->
                avionPort.findById(vol.avionId!!)
                    .map { avion -> vol to avion }
            }

    // Pousse un vol vers le partenaire si nécessaire
    fun pushToPartner(vol: Vol): Mono<Unit> {
        if (vol.destination != partnerAirportCode)
            return Mono.just(Unit)

        return avionPort.findById(vol.avionId!!)
            .flatMap { avion ->
                val req = SharedVolOutboundMapper.toRequest(vol, avion)
                remotePort.sendVol(req).thenReturn(Unit)
            }
    }

    // Synchronise les vols entrants depuis le partenaire
    fun syncIncomingFlights(): Flux<Vol> =
        remotePort.receiveVols()
            // On prend uniquement les vols à destination de notre aéroport
            .filter { shared -> shared.destination == myAirportCode }
            .flatMap { shared ->

                // Convert remote response → domain models
                val (avion, vol) = SharedVolInboundMapper.fromResponse(shared)
                import(avion, vol)
            }

}
