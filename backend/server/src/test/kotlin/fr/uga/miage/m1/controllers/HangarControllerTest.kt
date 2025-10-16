package fr.uga.miage.m1.controllers

import com.ninjasquad.springmockk.MockkBean
import fr.uga.miage.m1.requests.CreateHangarRequest
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.models.Hangar
import fr.uga.miage.m1.services.HangarService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(controllers = [HangarController::class])
class HangarControllerTest {

    @Autowired lateinit var client: WebTestClient

    @MockkBean lateinit var service: HangarService

    @Test
    fun `GET list returns 200`() {
        val h1 = Hangar(identifiant = "H1", capacite = 10, etat = HangarEtat.DISPONIBLE)
        every { service.list() } returns Flux.just(h1)

        client.get().uri("/api/hangars")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].identifiant").isEqualTo("H1")
    }

    @Test
    fun `POST create returns 201`() {
        val h2 = Hangar(identifiant = "H2", capacite = 20, etat = HangarEtat.DISPONIBLE)
        every { service.create(any<CreateHangarRequest>()) } returns Mono.just(h2)

        client.post().uri("/api/hangars")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"identifiant":"H2","capacite":20,"etat":"DISPONIBLE"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.identifiant").isEqualTo("H2")
    }
}
