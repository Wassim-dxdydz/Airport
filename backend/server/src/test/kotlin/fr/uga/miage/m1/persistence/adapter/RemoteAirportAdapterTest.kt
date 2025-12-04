package fr.uga.miage.m1.persistence.adapter

import fr.uga.miage.m1.requests.remote.SharedVolRequest
import fr.uga.miage.m1.responses.remote.SharedVolResponse
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

class RemoteAirportAdapterTest {

    private lateinit var client: RemoteAirportClient
    private lateinit var adapter: RemoteAirportAdapter

    @BeforeEach
    fun setup() {
        client = mockk()
        adapter = RemoteAirportAdapter(client)
    }

    @Test
    fun `sendVol delegates to client`() {
        val req = mockk<SharedVolRequest>()
        every { client.sendVolToPartner(req) } returns Mono.empty()

        StepVerifier.create(adapter.sendVol(req))
            .expectNext(Unit)
            .verifyComplete()

        verify { client.sendVolToPartner(req) }
    }

    @Test
    fun `fetchFlights delegates to client`() {
        val airport = "ALG"
        val response = mockk<SharedVolResponse>()

        every { client.fetchPartnerFlights(airport) } returns Flux.just(response)

        StepVerifier.create(adapter.fetchFlights(airport))
            .expectNext(response)
            .verifyComplete()

        verify { client.fetchPartnerFlights(airport) }
    }
}
