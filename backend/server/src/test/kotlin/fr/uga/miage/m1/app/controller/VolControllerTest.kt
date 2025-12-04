package fr.uga.miage.m1.app.controller

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.config.MockServiceConfig
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.service.VolService
import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.requests.UpdateVolRequest
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
    "spring.sql.init.mode=never",
    "local.airport.code=TEST"
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
            avionId = null,
            pisteId = null
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

        val created = Vol(
            id = UUID.randomUUID(),
            numeroVol = req.numeroVol,
            origine = req.origine,
            destination = req.destination,
            heureDepart = req.heureDepart,
            heureArrivee = req.heureArrivee,
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

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
    fun `GET vol by id`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        val v = Vol(
            id = id,
            numeroVol = "AF123",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        every { volService.get(id) } returns Mono.just(v)

        client.get().uri("$base/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody(VolResponse::class.java)

        verify { volService.get(id) }
    }

    @Test
    fun `PATCH update vol`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        val existing = Vol(
            id = id,
            numeroVol = "AF123",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        val req = UpdateVolRequest(destination = "JFK")
        val updated = existing.copy(destination = "JFK")

        every { volService.get(id) } returns Mono.just(existing)
        every { volService.updateBasicFields(id, any()) } returns Mono.just(updated)

        client.patch().uri("$base/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isOk
            .expectBody(VolResponse::class.java)

        verify { volService.updateBasicFields(id, any()) }
    }

    @Test
    fun `DELETE vol`() {
        val id = UUID.randomUUID()

        every { volService.delete(id) } returns Mono.empty()

        client.delete().uri("$base/$id")
            .exchange()
            .expectStatus().isNoContent

        verify { volService.delete(id) }
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
            avionId = avionId,
            pisteId = null
        )

        every { volService.assignAvion(id, avionId) } returns Mono.just(updated)

        client.post().uri("$base/$id/assign-avion/$avionId")
            .exchange()
            .expectStatus().isOk

        verify { volService.assignAvion(id, avionId) }
    }

    @Test
    fun `POST unassign avion`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        val updated = Vol(
            id = id,
            numeroVol = "AF123",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        every { volService.unassignAvion(id) } returns Mono.just(updated)

        client.post().uri("$base/$id/unassign-avion")
            .exchange()
            .expectStatus().isOk

        verify { volService.unassignAvion(id) }
    }

    @Test
    fun `PATCH update vol etat`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        val updated = Vol(
            id = id,
            numeroVol = "AF123",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.DECOLLE,
            avionId = null,
            pisteId = null
        )

        every { volService.updateEtat(id, VolEtat.DECOLLE) } returns Mono.just(updated)

        client.patch().uri("$base/$id/etat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(VolEtat.DECOLLE)
            .exchange()
            .expectStatus().isOk
            .expectBody(VolResponse::class.java)

        verify { volService.updateEtat(id, VolEtat.DECOLLE) }
    }

    @Test
    fun `GET vols by etat`() {
        val now = LocalDateTime.now()

        val v = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF321",
            origine = "LYS",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        every { volService.listByEtat(VolEtat.PREVU) } returns Flux.just(v)

        client.get().uri("$base/etat/PREVU")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(VolResponse::class.java)
            .hasSize(1)

        verify { volService.listByEtat(VolEtat.PREVU) }
    }

    @Test
    fun `POST assign piste`() {
        val id = UUID.randomUUID()
        val pisteId = UUID.randomUUID()
        val now = LocalDateTime.now()

        val updated = Vol(
            id = id,
            numeroVol = "AF001",
            origine = "ALG",
            destination = "TUN",
            heureDepart = now,
            heureArrivee = now.plusHours(1),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = pisteId
        )

        every { volService.assignPiste(id, pisteId) } returns Mono.just(updated)

        client.post().uri("$base/$id/assign-piste/$pisteId")
            .exchange()
            .expectStatus().isOk

        verify { volService.assignPiste(id, pisteId) }
    }

    @Test
    fun `POST release piste`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        val updated = Vol(
            id = id,
            numeroVol = "AF002",
            origine = "ALG",
            destination = "TUN",
            heureDepart = now,
            heureArrivee = now.plusHours(1),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        every { volService.releasePiste(id) } returns Mono.just(updated)

        client.post().uri("$base/$id/release-piste")
            .exchange()
            .expectStatus().isOk

        verify { volService.releasePiste(id) }
    }

    @Test
    fun `GET list departures`() {
        val now = LocalDateTime.now()

        val v = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF003",
            origine = "TEST",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        every { volService.listDeparturesFrom("TEST") } returns Flux.just(v)

        client.get().uri("$base/departures")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(VolResponse::class.java)

        verify { volService.listDeparturesFrom("TEST") }
    }

    @Test
    fun `GET list arrivals`() {
        val now = LocalDateTime.now()

        val v = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF004",
            origine = "PAR",
            destination = "TEST",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        every { volService.listArrivalsTo("TEST") } returns Flux.just(v)

        client.get().uri("$base/arrivals")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(VolResponse::class.java)

        verify { volService.listArrivalsTo("TEST") }
    }
}
