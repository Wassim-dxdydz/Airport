package fr.uga.miage.m1.app.controller

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import com.ninjasquad.springmockk.MockkBean
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.domain.port.HangarDataPort
import fr.uga.miage.m1.domain.service.AvionService
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.requests.CreateAvionRequest
import fr.uga.miage.m1.requests.UpdateAvionRequest
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

@WebFluxTest(AvionController::class)
class AvionControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var avionService: AvionService

    @MockkBean
    private lateinit var hangarPort: HangarDataPort

    @Test
    fun `GET avions returns list with hangar identifiants`() {
        val hangarId = UUID.randomUUID()
        val avion1 = Avion(UUID.randomUUID(), "F-GRNB", "A320", 180, AvionEtat.DISPONIBLE, hangarId)
        val avion2 = Avion(UUID.randomUUID(), "F-TEST", "A330", 250, AvionEtat.EN_VOL, null)
        val hangar = Hangar(hangarId, "H1", 10, backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat.DISPONIBLE)

        every { avionService.list() } returns Flux.just(avion1, avion2)
        every { hangarPort.findAllByIds(setOf(hangarId)) } returns Flux.just(hangar)

        webTestClient.get()
            .uri("/avions")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Any::class.java)
            .hasSize(2)

        verify { avionService.list() }
        verify { hangarPort.findAllByIds(setOf(hangarId)) }
    }

    @Test
    fun `GET avions returns list without hangars`() {
        val avion = Avion(UUID.randomUUID(), "F-GRNB", "A320", 180, AvionEtat.EN_VOL, null)

        every { avionService.list() } returns Flux.just(avion)

        webTestClient.get()
            .uri("/avions")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { avionService.list() }
    }

    @Test
    fun `GET avions by id returns avion with hangar`() {
        val id = UUID.randomUUID()
        val hangarId = UUID.randomUUID()
        val avion = Avion(id, "F-GRNB", "A320", 180, AvionEtat.DISPONIBLE, hangarId)
        val hangar = Hangar(hangarId, "H1", 10, backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat.DISPONIBLE)

        every { avionService.get(id) } returns Mono.just(avion)
        every { hangarPort.findById(hangarId) } returns Mono.just(hangar)

        webTestClient.get()
            .uri("/avions/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.immatriculation").isEqualTo("F-GRNB")
            .jsonPath("$.hangarIdentifiant").isEqualTo("H1")

        verify { avionService.get(id) }
        verify { hangarPort.findById(hangarId) }
    }

    @Test
    fun `GET avions by id returns 404 when not found`() {
        val id = UUID.randomUUID()

        every { avionService.get(id) } returns Mono.error(NotFoundException("Avion $id non trouvé"))

        webTestClient.get()
            .uri("/avions/$id")
            .exchange()
            .expectStatus().isNotFound

        verify { avionService.get(id) }
    }

    @Test
    fun `POST avions creates new avion`() {
        val request = CreateAvionRequest(
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.DISPONIBLE,
            hangarId = null
        )
        val created = Avion(UUID.randomUUID(), "F-GRNB", "A320", 180, AvionEtat.DISPONIBLE, null)

        every { avionService.create(any()) } returns Mono.just(created)

        webTestClient.post()
            .uri("/avions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.immatriculation").isEqualTo("F-GRNB")
            .jsonPath("$.type").isEqualTo("A320")

        verify { avionService.create(any()) }
    }

    @Test
    fun `PATCH avions updates existing avion`() {
        val id = UUID.randomUUID()
        val current = Avion(id, "F-GRNB", "A320", 180, AvionEtat.DISPONIBLE, null)
        val request = UpdateAvionRequest(
            type = "A321",
            capacite = 200,
            etat = null
        )
        val updated = current.copy(type = "A321", capacite = 200)

        every { avionService.get(id) } returns Mono.just(current)
        every { avionService.update(id, any()) } returns Mono.just(updated)

        webTestClient.patch()
            .uri("/avions/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.type").isEqualTo("A321")
            .jsonPath("$.capacite").isEqualTo(200)

        verify { avionService.get(id) }
        verify { avionService.update(id, any()) }
    }

    @Test
    fun `DELETE avions removes avion`() {
        val id = UUID.randomUUID()

        every { avionService.delete(id) } returns Mono.just(Unit)

        webTestClient.delete()
            .uri("/avions/$id")
            .exchange()
            .expectStatus().isOk

        verify { avionService.delete(id) }
    }

    @Test
    fun `POST avions assign hangar succeeds`() {
        val avionId = UUID.randomUUID()
        val hangarId = UUID.randomUUID()
        val updated = Avion(avionId, "F-GRNB", "A320", 180, AvionEtat.DISPONIBLE, hangarId)

        every { avionService.assignHangar(avionId, hangarId) } returns Mono.just(updated)

        webTestClient.post()
            .uri("/avions/$avionId/hangar/$hangarId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.hangarId").isEqualTo(hangarId.toString())

        verify { avionService.assignHangar(avionId, hangarId) }
    }

    @Test
    fun `DELETE avions hangar unassigns hangar`() {
        val id = UUID.randomUUID()
        val updated = Avion(id, "F-GRNB", "A320", 180, AvionEtat.EN_VOL, null)

        every { avionService.unassignHangar(id) } returns Mono.just(updated)

        webTestClient.delete()
            .uri("/avions/$id/hangar")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.hangarId").isEmpty

        verify { avionService.unassignHangar(id) }
    }
}
