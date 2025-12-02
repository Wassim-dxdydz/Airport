package fr.uga.miage.m1.app.controller

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.app.mapper.HangarMapper
import fr.uga.miage.m1.config.MockServiceConfig
import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.service.HangarService
import fr.uga.miage.m1.requests.CreateHangarRequest
import fr.uga.miage.m1.requests.UpdateHangarRequest
import fr.uga.miage.m1.responses.HangarResponse
import fr.uga.miage.m1.responses.AvionResponse
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
class HangarControllerTest(
    @Autowired private val client: WebTestClient,
    @Autowired private val hangarService: HangarService
) {

    private val base = "/api/hangars"

    @Test
    fun `GET all hangars`() {
        val h = Hangar(
            id = UUID.randomUUID(),
            identifiant = "H1",
            capacite = 10,
            etat = HangarEtat.DISPONIBLE
        )

        every { hangarService.list() } returns Flux.just(h)

        client.get().uri(base)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(HangarResponse::class.java)
            .hasSize(1)

        verify { hangarService.list() }
    }

    @Test
    fun `GET hangar by id`() {
        val id = UUID.randomUUID()
        val h = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)

        every { hangarService.get(id) } returns Mono.just(h)

        client.get().uri("$base/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody(HangarResponse::class.java)
            .consumeWith {
                assert(it.responseBody!!.identifiant == "H1")
            }

        verify { hangarService.get(id) }
    }

    @Test
    fun `POST create hangar`() {
        val req = CreateHangarRequest("H1", 10, HangarEtat.DISPONIBLE)
        val saved = HangarMapper.toDomain(req).copy(id = UUID.randomUUID())

        every { hangarService.create(any()) } returns Mono.just(saved)

        client.post().uri(base)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated
            .expectBody(HangarResponse::class.java)

        verify { hangarService.create(any()) }
    }

    @Test
    fun `PUT update hangar`() {
        val id = UUID.randomUUID()
        val req = UpdateHangarRequest(capacite = 20, etat = null)
        val updated = Hangar(id, "H1", 20, HangarEtat.DISPONIBLE)

        every { hangarService.get(id) } returns Mono.just(updated.copy(capacite = 10))
        every { hangarService.update(id, any()) } returns Mono.just(updated)

        client.put().uri("$base/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isOk

        verify { hangarService.update(id, any()) }
    }

    @Test
    fun `GET list avions in hangar`() {
        val id = UUID.randomUUID()
        val avion = Avion(
            id = UUID.randomUUID(),
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat.EN_SERVICE,
            hangarId = id
        )

        every { hangarService.listAvions(id) } returns Flux.just(avion)

        client.get().uri("$base/$id/avions")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(AvionResponse::class.java)
            .hasSize(1)

        verify { hangarService.listAvions(id) }
    }

    @Test
    fun `DELETE hangar`() {
        val id = UUID.randomUUID()

        every { hangarService.delete(id) } returns Mono.empty()

        client.delete().uri("$base/$id")
            .exchange()
            .expectStatus().isNoContent   // Because @ResponseStatus(HttpStatus.NO_CONTENT)

        verify { hangarService.delete(id) }
    }

}
