package fr.uga.miage.m1.adapter

import fr.uga.miage.m1.ApiClient
import fr.uga.miage.m1.models.VolDto
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux

@Component
class WebClientAdapter(
    private val baseUrl: String = "http://129.88.210.36:8080/api/public/vols"
) : ApiClient {

    private val webClient: WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    override fun getDeparturesFromATL(): Flux<VolDto> {
        return webClient.get()
            .uri("/departures/ATL")
            .retrieve()
            .bodyToFlux<VolDto>()
            .doOnError { e ->
                println("error from remote service: ${e.message}")
                e.printStackTrace()
            }
    }
}
