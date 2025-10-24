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
    fun `GET by id returns 200 with payload`() {
        val id = java.util.UUID.randomUUID()
        val avion = Avion(
            immatriculation = "F-XYZ1",
            type = "B737",
            capacite = 160,
            etat = AvionEtat.EN_SERVICE
        )
        every { service.get(id) } returns Mono.just(avion)

        client.get().uri("/api/avions/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.immatriculation").isEqualTo("F-XYZ1")
            .jsonPath("$.type").isEqualTo("B737")
            .jsonPath("$.capacite").isEqualTo(160)
            .jsonPath("$.etat").isEqualTo("EN_SERVICE")
    }

    @Test
    fun `PUT update returns 200 with updated payload`() {
        val id = java.util.UUID.randomUUID()
        val updated = Avion(
            immatriculation = "F-UPDT",
            type = "A321",
            capacite = 200,
            etat = AvionEtat.EN_SERVICE
        )
        every { service.update(eq(id), any()) } returns Mono.just(updated)

        client.put().uri("/api/avions/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
            {
              "immatriculation":"F-UPDT",
              "type":"A321",
              "capacite":200,
              "etat":"EN_SERVICE",
              "hangarId": null
            }
            """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.immatriculation").isEqualTo("F-UPDT")
            .jsonPath("$.type").isEqualTo("A321")
            .jsonPath("$.capacite").isEqualTo(200)
            .jsonPath("$.etat").isEqualTo("EN_SERVICE")
    }

    @Test
    fun `DELETE returns 204`() {
        val id = java.util.UUID.randomUUID()
        every { service.delete(id) } returns Mono.empty()

        client.delete().uri("/api/avions/$id")
            .exchange()
            .expectStatus().isNoContent
            .expectBody().isEmpty
    }

    @Test
    fun `POST assignHangar returns 200 with hangar set`() {
        val id = java.util.UUID.randomUUID()
        val hangarId = java.util.UUID.randomUUID()
        val withHangar = Avion(
            immatriculation = "F-HANG",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = hangarId
        )
        every { service.assignHangar(id, hangarId) } returns Mono.just(withHangar)

        client.post().uri("/api/avions/$id/assign-hangar/$hangarId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.immatriculation").isEqualTo("F-HANG")
            .jsonPath("$.hangarId").isEqualTo(hangarId.toString())
    }

    @Test
    fun `POST unassignHangar returns 200 with hangar cleared`() {
        val id = java.util.UUID.randomUUID()
        val withoutHangar = Avion(
            immatriculation = "F-UNHG",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = null
        )
        every { service.unassignHangar(id) } returns Mono.just(withoutHangar)

        client.post().uri("/api/avions/$id/unassign-hangar")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.immatriculation").isEqualTo("F-UNHG")
            .jsonPath("$.hangarId").doesNotExist()
    }

}
