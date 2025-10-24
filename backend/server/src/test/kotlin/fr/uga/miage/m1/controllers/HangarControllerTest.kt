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
    fun `GET by id returns 200 with payload`() {
        val id = java.util.UUID.randomUUID()
        val hangar = Hangar(identifiant = "H42", capacite = 12, etat = HangarEtat.DISPONIBLE)
        every { service.get(id) } returns Mono.just(hangar)

        client.get().uri("/api/hangars/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.identifiant").isEqualTo("H42")
            .jsonPath("$.capacite").isEqualTo(12)
            .jsonPath("$.etat").isEqualTo("DISPONIBLE")
    }

    @Test
    fun `PUT update returns 200 with updated payload`() {
        val id = java.util.UUID.randomUUID()
        val updated = Hangar(identifiant = "H-UP", capacite = 30, etat = HangarEtat.DISPONIBLE)
        every { service.update(eq(id), any()) } returns Mono.just(updated)

        client.put().uri("/api/hangars/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
            {
              "identifiant":"H-UP",
              "capacite":30,
              "etat":"DISPONIBLE"
            }
            """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.identifiant").isEqualTo("H-UP")
            .jsonPath("$.capacite").isEqualTo(30)
            .jsonPath("$.etat").isEqualTo("DISPONIBLE")
    }

    @Test
    fun `DELETE returns 204`() {
        val id = java.util.UUID.randomUUID()
        every { service.delete(id) } returns Mono.empty()

        client.delete().uri("/api/hangars/$id")
            .exchange()
            .expectStatus().isNoContent
            .expectBody().isEmpty
    }

    @Test
    fun `GET listAvions returns 200 with payload`() {
        val id = java.util.UUID.randomUUID()

        // Using Avion model + AvionEtat for the listAvions payload
        val avion1 = fr.uga.miage.m1.models.Avion(
            immatriculation = "F-AV1",
            type = "A320",
            capacite = 180,
            etat = backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat.EN_SERVICE,
            hangarId = id
        )
        val avion2 = fr.uga.miage.m1.models.Avion(
            immatriculation = "F-AV2",
            type = "B737",
            capacite = 160,
            etat = backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat.MAINTENANCE,
            hangarId = id
        )

        every { service.listAvions(id) } returns Flux.just(avion1, avion2)

        client.get().uri("/api/hangars/$id/avions")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].immatriculation").isEqualTo("F-AV1")
            .jsonPath("$[0].type").isEqualTo("A320")
            .jsonPath("$[0].capacite").isEqualTo(180)
            .jsonPath("$[0].etat").isEqualTo("EN_SERVICE")
            .jsonPath("$[1].immatriculation").isEqualTo("F-AV2")
            .jsonPath("$[1].etat").isEqualTo("MAINTENANCE")
    }
}
