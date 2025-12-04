package fr.uga.miage.m1.domain.service

import fr.uga.miage.m1.app.mapper.SharedVolInboundMapper
import fr.uga.miage.m1.app.mapper.SharedVolOutboundMapper
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.RemoteAirportPort
import fr.uga.miage.m1.domain.port.VolDataPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class SharedVolSyncService(
    private val avionPort: AvionDataPort,
    private val volPort: VolDataPort,
    private val remotePort: RemoteAirportPort,
    @Value("\${local.airport.code}") private val myAirportCode: String,
    @Value("\${remote.airport.code}") private val partnerAirportCode: String
) {
    // Importer un vol avec son avion associé
    fun import(avion: Avion, vol: Vol): Mono<Vol> {
        val avionMono = avionPort.findByImmatriculation(avion.immatriculation)
            .switchIfEmpty(avionPort.save(avion))

        return avionMono.flatMap { persisted ->
            val volToStore = vol.copy(avionId = persisted.id)

            volPort.findByNumeroVol(vol.numeroVol)
                .flatMap { existing ->
                    val merged = existing.copy(
                        origine = volToStore.origine,
                        destination = volToStore.destination,
                        heureDepart = volToStore.heureDepart,
                        heureArrivee = volToStore.heureArrivee,
                        etat = volToStore.etat,
                        avionId = persisted.id
                    )
                    volPort.save(merged)
                }
                .switchIfEmpty(volPort.save(volToStore))
        }
    }

    // Exporter les vols dont la destination correspond à l'aéroport partenaire
    fun exportForPartner(): Flux<Pair<Vol, Avion>> =
        volPort.findByDestination(partnerAirportCode)
            .flatMap { vol ->
                avionPort.findById(vol.avionId!!)
                    .map { avion -> vol to avion }
            }

    // Pousser un vol vers l'aéroport partenaire si sa destination correspond
    fun pushToPartner(vol: Vol): Mono<Unit> {
        if (vol.destination != partnerAirportCode)
            return Mono.just(Unit)

        return avionPort.findById(vol.avionId!!)
            .flatMap { avion ->
                val req = SharedVolOutboundMapper.toRequest(vol, avion)
                remotePort.sendVol(req).thenReturn(Unit)
            }
    }
    // Synchronizer les vols entrants depuis l'aéroport partenaire
    fun syncIncomingFlights(): Flux<Vol> =
        remotePort.fetchFlights(myAirportCode)
            .flatMap { shared ->
                val (avion, vol) = SharedVolInboundMapper.fromResponse(shared)
                import(avion, vol)
            }
}
