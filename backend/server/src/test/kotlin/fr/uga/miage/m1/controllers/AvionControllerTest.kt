package fr.uga.miage.m1.controllers

import fr.uga.miage.m1.requests.CreateAvionRequest
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.models.Avion
import fr.uga.miage.m1.services.AvionService
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import com.ninjasquad.springmockk.MockkBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(controllers = [AvionController::class])
@Import(AvionController::class)
class AvionControllerTest {

    @Autowired lateinit var client: WebTestClient

    @MockkBean lateinit var service: AvionService

    @Test
    fun `GET list returns 200 with payload`() {
        val a1 = Avion(immatriculation="F-ABCD", type="A320", capacite=180, etat=AvionEtat.EN_SERVICE)
        every { service.list() } returns Flux.just(a1)

        client.get().uri("/api/avions")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].immatriculation").isEqualTo("F-ABCD")
    }

    @Test
    fun `POST create returns 201`() {
        val saved = Avion(immatriculation="F-NEW", type="A320", capacite=180, etat=AvionEtat.EN_SERVICE)
        every { service.create(any<CreateAvionRequest>()) } returns Mono.just(saved)

        client.post().uri("/api/avions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"immatriculation":"F-NEW","type":"A320","capacite":180,"etat":"EN_SERVICE"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.immatriculation").isEqualTo("F-NEW")
    }
}
