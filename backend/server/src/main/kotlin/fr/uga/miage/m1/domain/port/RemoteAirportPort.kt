package fr.uga.miage.m1.domain.port

import fr.uga.miage.m1.requests.remote.SharedVolRequest
import fr.uga.miage.m1.responses.remote.SharedVolResponse
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

interface RemoteAirportPort {
    fun sendVol(req: SharedVolRequest): Mono<Unit>
    fun fetchFlights(airportCode: String): Flux<SharedVolResponse>
}
