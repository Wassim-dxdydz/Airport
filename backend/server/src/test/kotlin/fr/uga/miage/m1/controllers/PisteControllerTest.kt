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

    @Test
    fun `GET by id returns 200`() {
        val id = java.util.UUID.randomUUID()
        val piste = Piste(identifiant = "R3", longueurM = 3000, etat = PisteEtat.MAINTENANCE)
        every { service.get(id) } returns Mono.just(piste)

        client.get().uri("/api/pistes/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.identifiant").isEqualTo("R3")
            .jsonPath("$.etat").isEqualTo("MAINTENANCE")
    }

    @Test
    fun `PUT updateEtat returns 200`() {
        val id = java.util.UUID.randomUUID()
        val updated = Piste(identifiant = "R4", longueurM = 3100, etat = PisteEtat.OCCUPEE)
        every { service.updateEtat(eq(id), any()) } returns Mono.just(updated)

        client.put().uri("/api/pistes/$id/etat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"etat":"OCCUPEE"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.identifiant").isEqualTo("R4")
            .jsonPath("$.longueurM").isEqualTo(3100)
            .jsonPath("$.etat").isEqualTo("OCCUPEE")
    }


    @Test
    fun `DELETE returns 204`() {
        val id = java.util.UUID.randomUUID()
        every { service.delete(id) } returns Mono.empty()

        client.delete().uri("/api/pistes/$id")
            .exchange()
            .expectStatus().isNoContent
            .expectBody().isEmpty
    }

    @Test
    fun `GET disponibles returns 200 with available pistes`() {
        val p1 = Piste(identifiant = "R5", longueurM = 3300, etat = PisteEtat.LIBRE)
        val p2 = Piste(identifiant = "R6", longueurM = 3600, etat = PisteEtat.LIBRE)
        every { service.disponibles() } returns Flux.just(p1, p2)

        client.get().uri("/api/pistes/disponibles")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].etat").isEqualTo("LIBRE")
            .jsonPath("$[1].etat").isEqualTo("LIBRE")
    }
}
