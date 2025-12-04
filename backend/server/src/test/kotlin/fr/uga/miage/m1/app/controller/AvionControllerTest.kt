package fr.uga.miage.m1.app.controller

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.config.MockServiceConfig
import fr.uga.miage.m1.domain.service.AvionService
import fr.uga.miage.m1.requests.CreateAvionRequest
import fr.uga.miage.m1.responses.AvionResponse
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.app.mapper.AvionMapper
import fr.uga.miage.m1.requests.UpdateAvionRequest
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(MockServiceConfig::class)
@TestPropertySource(properties = [
    "spring.sql.init.mode=never"
])
class AvionControllerTest(
    @Autowired private val client: WebTestClient,
    @Autowired private val avionService: AvionService
) {

    private val baseUrl = "/api/avions"

    @Test
    fun `GET all avions`() {
        val a = Avion(
            id = UUID.randomUUID(),
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = null
        )

        every { avionService.list() } returns Flux.just(a)

        client.get().uri(baseUrl)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(AvionResponse::class.java)
            .hasSize(1)

        verify { avionService.list() }
    }

    @Test
    fun `GET avion by id`() {
        val id = UUID.randomUUID()
        val avion = Avion(id, "F-GRNB", "A320", 180, AvionEtat.EN_SERVICE, null)

        every { avionService.get(id) } returns Mono.just(avion)

        client.get().uri("$baseUrl/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody(AvionResponse::class.java)
            .consumeWith {
                assert(it.responseBody!!.immatriculation == "F-GRNB")
            }

        verify { avionService.get(id) }
    }

    @Test
    fun `POST create avion`() {
        val req = CreateAvionRequest("F-GRNB", "A320", 180, AvionEtat.EN_SERVICE, null)
        val saved = AvionMapper.toDomain(req).copy(id = UUID.randomUUID())

        every { avionService.create(any()) } returns Mono.just(saved)

        client.post().uri(baseUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated
            .expectBody(AvionResponse::class.java)

        verify { avionService.create(any()) }
    }

    @Test
    fun `PATCH update avion`() {
        val id = UUID.randomUUID()

        val existing = Avion(
            id = id,
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = null
        )

        val req = UpdateAvionRequest(type = "A321", capacite = 200)

        val updated = existing.copy(type = "A321", capacite = 200)

        every { avionService.get(id) } returns Mono.just(existing)
        every { avionService.update(id, any()) } returns Mono.just(updated)

        client.patch().uri("$baseUrl/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isOk
            .expectBody(AvionResponse::class.java)
            .consumeWith {
                assert(it.responseBody!!.type == "A321")
            }

        verify { avionService.update(id, any()) }
    }

    @Test
    fun `DELETE avion`() {
        val id = UUID.randomUUID()

        every { avionService.delete(id) } returns Mono.empty()

        client.delete().uri("$baseUrl/$id")
            .exchange()
            .expectStatus().isNoContent

        verify { avionService.delete(id) }
    }

    @Test
    fun `POST assign hangar to avion`() {
        val id = UUID.randomUUID()
        val hangarId = UUID.randomUUID()

        val updated = Avion(
            id = id,
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = hangarId
        )

        every { avionService.assignHangar(id, hangarId) } returns Mono.just(updated)

        client.post().uri("$baseUrl/$id/assign-hangar/$hangarId")
            .exchange()
            .expectStatus().isOk
            .expectBody(AvionResponse::class.java)
            .consumeWith { assert(it.responseBody!!.hangarId == hangarId) }

        verify { avionService.assignHangar(id, hangarId) }
    }

    @Test
    fun `POST unassign hangar from avion`() {
        val id = UUID.randomUUID()

        val updated = Avion(
            id = id,
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = null
        )

        every { avionService.unassignHangar(id) } returns Mono.just(updated)

        client.post().uri("$baseUrl/$id/unassign-hangar")
            .exchange()
            .expectStatus().isOk
            .expectBody(AvionResponse::class.java)
            .consumeWith { assert(it.responseBody!!.hangarId == null) }

        verify { avionService.unassignHangar(id) }
    }

}
