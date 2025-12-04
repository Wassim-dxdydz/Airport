package fr.uga.miage.m1.persistence.adapter

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.requests.remote.SharedVolRequest
import fr.uga.miage.m1.responses.remote.SharedVolResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class RemoteAirportClientTest {

    private lateinit var webClient: WebClient
    private lateinit var client: RemoteAirportClient

    private lateinit var requestBodyUriSpec: RequestBodyUriSpec
    private lateinit var requestHeadersUriSpec: RequestHeadersUriSpec<*>
    private lateinit var responseSpec: ResponseSpec

    @BeforeEach
    fun setup() {
        webClient = mockk()
        requestBodyUriSpec = mockk()
        requestHeadersUriSpec = mockk()
        responseSpec = mockk()

        client = RemoteAirportClient(webClient)
    }

    @Test
    fun `sendVolToPartner does POST to import endpoint`() {
        val req = mockk<SharedVolRequest>()

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("/api/shared/vols/import") } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(req) } returns requestBodyUriSpec
        every { requestBodyUriSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(Void::class.java) } returns Mono.empty()

        StepVerifier.create(client.sendVolToPartner(req))
            .verifyComplete()

        verifyOrder {
            webClient.post()
            requestBodyUriSpec.uri("/api/shared/vols/import")
            requestBodyUriSpec.bodyValue(req)
            requestBodyUriSpec.retrieve()
            responseSpec.bodyToMono(Void::class.java)
        }
    }

    @Test
    fun `fetchPartnerFlights does GET to partner endpoint`() {
        val now = LocalDateTime.now()

        val response = SharedVolResponse(
            numeroVol = "AF100",
            origine = "CDG",
            destination = "ALG",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionImmatriculation = "F-TEST",
            avionType = "A320",
            avionCapacite = 180,
            avionEtat = AvionEtat.EN_SERVICE
        )

        val airportCode = "ALG"

        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("/api/volExterieurs/{code}", airportCode) } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToFlux(SharedVolResponse::class.java) } returns Flux.just(response)

        StepVerifier.create(client.fetchPartnerFlights(airportCode))
            .expectNext(response)
            .verifyComplete()

        verifyOrder {
            webClient.get()
            requestHeadersUriSpec.uri("/api/volExterieurs/{code}", airportCode)
            requestHeadersUriSpec.retrieve()
            responseSpec.bodyToFlux(SharedVolResponse::class.java)
        }
    }
}
