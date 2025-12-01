package fr.uga.miage.m1.app.controller

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.app.mapper.PisteMapper
import fr.uga.miage.m1.config.MockServiceConfig
import fr.uga.miage.m1.domain.model.Piste
import fr.uga.miage.m1.domain.service.PisteService
import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteEtatRequest
import fr.uga.miage.m1.responses.PisteResponse
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
class PisteControllerTest(
    @Autowired private val client: WebTestClient,
    @Autowired private val pisteService: PisteService
) {

    private val base = "/api/pistes"

    @Test
    fun `GET all pistes`() {
        val p = Piste(UUID.randomUUID(), "R1", 3200, PisteEtat.LIBRE)

        every { pisteService.list() } returns Flux.just(p)

        client.get().uri(base)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(PisteResponse::class.java)
            .hasSize(1)

        verify { pisteService.list() }
    }

    @Test
    fun `POST create piste`() {
        val req = CreatePisteRequest("R1", 3200, PisteEtat.LIBRE)
        val saved = PisteMapper.toDomain(req).copy(id = UUID.randomUUID())

        every { pisteService.create(any()) } returns Mono.just(saved)

        client.post().uri(base)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated
            .expectBody(PisteResponse::class.java)

        verify { pisteService.create(any()) }
    }

    @Test
    fun `PATCH update etat`() {
        val id = UUID.randomUUID()
        val req = UpdatePisteEtatRequest(PisteEtat.OCCUPEE)
        val updated = Piste(id, "R1", 3200, PisteEtat.OCCUPEE)

        every { pisteService.get(id) } returns Mono.just(updated.copy(etat = PisteEtat.LIBRE))
        every { pisteService.updateEtat(id, req.etat) } returns Mono.just(updated)

        client.patch().uri("$base/$id/etat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isOk

        verify { pisteService.updateEtat(id, req.etat) }
    }
}
