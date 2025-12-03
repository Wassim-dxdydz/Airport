package fr.uga.miage.m1.persistence.adapter

import fr.uga.miage.m1.requests.remote.SharedVolRequest
import fr.uga.miage.m1.responses.remote.SharedVolResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class RemoteAirportClient(
    private val remoteAirportWebClient: WebClient
) {

    fun importVol(req: SharedVolRequest): Mono<Void> =
        remoteAirportWebClient.post()
            .uri("/api/shared/vols/import")
            .bodyValue(req)
            .retrieve()
            .bodyToMono(Void::class.java)

    fun fetchRemoteVols(): Flux<SharedVolResponse> =
        remoteAirportWebClient.get()
            .uri("/api/shared/vols/export")
            .retrieve()
            .bodyToFlux(SharedVolResponse::class.java)
}
