package fr.uga.miage.m1.app.controller

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.app.mapper.VolMapper
import fr.uga.miage.m1.config.MockServiceConfig
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.service.VolService
import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.responses.VolResponse
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
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(MockServiceConfig::class)
@TestPropertySource(properties = [
    "spring.sql.init.mode=never"
])
class VolControllerTest(
    @Autowired private val client: WebTestClient,
    @Autowired private val volService: VolService
) {

    private val base = "/api/vols"

    @Test
    fun `GET all vols`() {
        val now = LocalDateTime.now()
        val v = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF123",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null
        )

        every { volService.list() } returns Flux.just(v)

        client.get().uri(base)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(VolResponse::class.java)
            .hasSize(1)

        verify { volService.list() }
    }

    @Test
    fun `POST create vol`() {
        val now = LocalDateTime.now()
        val req = CreateVolRequest(
            numeroVol = "AF999",
            origine = "MAD",
            destination = "CDG",
            heureDepart = now.plusHours(2),
            heureArrivee = now.plusHours(4)
        )

        val created = VolMapper.toDomain(req).copy(id = UUID.randomUUID())

        every { volService.create(any()) } returns Mono.just(created)

        client.post().uri(base)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated
            .expectBody(VolResponse::class.java)

        verify { volService.create(any()) }
    }

    @Test
    fun `POST assign avion`() {
        val id = UUID.randomUUID()
        val avionId = UUID.randomUUID()
        val now = LocalDateTime.now()

        val updated = Vol(
            id = id,
            numeroVol = "AF888",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = avionId
        )

        every { volService.assignAvion(id, avionId) } returns Mono.just(updated)

        client.post().uri("$base/$id/assign-avion/$avionId")
            .exchange()
            .expectStatus().isOk

        verify { volService.assignAvion(id, avionId) }
    }
}
