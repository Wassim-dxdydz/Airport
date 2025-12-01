package fr.uga.miage.m1.app.controller

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.config.MockServiceConfig
import fr.uga.miage.m1.domain.service.AvionService
import fr.uga.miage.m1.requests.CreateAvionRequest
import fr.uga.miage.m1.responses.AvionResponse
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.app.mapper.AvionMapper
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
}
