package fr.uga.miage.m1.app.controller

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import com.ninjasquad.springmockk.MockkBean
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.domain.service.HangarService
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.requests.CreateHangarRequest
import fr.uga.miage.m1.requests.UpdateHangarRequest
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@WebFluxTest(HangarController::class)
class HangarControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var hangarService: HangarService

    @Test
    fun `GET hangars returns list`() {
        val hangar = Hangar(UUID.randomUUID(), "H1", 10, HangarEtat.DISPONIBLE)

        every { hangarService.list() } returns Flux.just(hangar)

        webTestClient.get()
            .uri("/hangars")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { hangarService.list() }
    }

    @Test
    fun `GET hangars by id returns hangar`() {
        val id = UUID.randomUUID()
        val hangar = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)

        every { hangarService.get(id) } returns Mono.just(hangar)

        webTestClient.get()
            .uri("/hangars/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.identifiant").isEqualTo("H1")
            .jsonPath("$.capacite").isEqualTo(10)
            .jsonPath("$.etat").isEqualTo("DISPONIBLE")

        verify { hangarService.get(id) }
    }

    @Test
    fun `GET hangars by id returns 404 when not found`() {
        val id = UUID.randomUUID()

        every { hangarService.get(id) } returns Mono.error(NotFoundException("Hangar $id non trouvé"))

        webTestClient.get()
            .uri("/hangars/$id")
            .exchange()
            .expectStatus().isNotFound

        verify { hangarService.get(id) }
    }

    @Test
    fun `POST hangars creates new hangar`() {
        val request = CreateHangarRequest(
            identifiant = "H2",
            capacite = 15,
            etat = HangarEtat.DISPONIBLE
        )
        val created = Hangar(UUID.randomUUID(), "H2", 15, HangarEtat.DISPONIBLE)

        every { hangarService.create(any()) } returns Mono.just(created)

        webTestClient.post()
            .uri("/hangars")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.identifiant").isEqualTo("H2")
            .jsonPath("$.capacite").isEqualTo(15)

        verify { hangarService.create(any()) }
    }

    @Test
    fun `PATCH hangars updates existing hangar`() {
        val id = UUID.randomUUID()
        val current = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)
        val request = UpdateHangarRequest(
            capacite = 20,
            etat = null
        )
        val updated = current.copy(capacite = 20)

        every { hangarService.get(id) } returns Mono.just(current)
        every { hangarService.update(id, any()) } returns Mono.just(updated)

        webTestClient.patch()
            .uri("/hangars/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.capacite").isEqualTo(20)

        verify { hangarService.get(id) }
        verify { hangarService.update(id, any()) }
    }

    @Test
    fun `DELETE hangars removes hangar`() {
        val id = UUID.randomUUID()

        every { hangarService.delete(id) } returns Mono.just(Unit)

        webTestClient.delete()
            .uri("/hangars/$id")
            .exchange()
            .expectStatus().isOk

        verify { hangarService.delete(id) }
    }

    @Test
    fun `GET hangars avions returns list of avions`() {
        val id = UUID.randomUUID()
        val avion = Avion(UUID.randomUUID(), "F-GRNB", "A320", 180, AvionEtat.DISPONIBLE, id)

        every { hangarService.listAvions(id) } returns Flux.just(avion)

        webTestClient.get()
            .uri("/hangars/$id/avions")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { hangarService.listAvions(id) }
    }
}
