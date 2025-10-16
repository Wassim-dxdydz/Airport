package fr.uga.miage.m1.controllers

import com.ninjasquad.springmockk.MockkBean
import fr.uga.miage.m1.requests.CreatePisteRequest
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.models.Piste
import fr.uga.miage.m1.services.PisteService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(controllers = [PisteController::class])
class PisteControllerTest {

    @Autowired lateinit var client: WebTestClient

    @MockkBean lateinit var service: PisteService

    @Test
    fun `GET list returns 200`() {
        val piste = Piste(identifiant = "R1", longueurM = 3200, etat = PisteEtat.LIBRE)
        every { service.list() } returns Flux.just(piste)

        client.get().uri("/api/pistes")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].identifiant").isEqualTo("R1")
    }

    @Test
    fun `POST create returns 201`() {
        val saved = Piste(identifiant = "R2", longueurM = 3500, etat = PisteEtat.LIBRE)
        every { service.create(any<CreatePisteRequest>()) } returns Mono.just(saved)

        client.post().uri("/api/pistes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"identifiant":"R2","longueurM":3500,"etat":"LIBRE"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.identifiant").isEqualTo("R2")
    }
}
