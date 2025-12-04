package fr.uga.miage.m1.persistence.adapter

import fr.uga.miage.m1.domain.port.RemoteAirportPort
import fr.uga.miage.m1.requests.remote.SharedVolRequest
import fr.uga.miage.m1.responses.remote.SharedVolResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class RemoteAirportAdapter(
    private val client: RemoteAirportClient
) : RemoteAirportPort {

    override fun sendVol(req: SharedVolRequest): Mono<Unit> =
        client.sendVolToPartner(req).thenReturn(Unit)

    override fun fetchFlights(airportCode: String): Flux<SharedVolResponse> =
        client.fetchPartnerFlights(airportCode)
}