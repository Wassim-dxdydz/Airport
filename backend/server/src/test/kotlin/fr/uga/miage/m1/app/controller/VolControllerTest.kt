package fr.uga.miage.m1.app.controller

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import com.ninjasquad.springmockk.MockkBean
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.service.VolService
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.models.VolDto
import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.requests.UpdateVolRequest
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@WebFluxTest(VolController::class)
@TestPropertySource(properties = ["airport.code=CDG"])
class VolControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var volService: VolService

    @Test
    fun `GET vols returns list`() {
        val now = LocalDateTime.now()
        val vol = Vol(
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

        every { volService.list() } returns Flux.just(vol)

        webTestClient.get()
            .uri("/vols")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { volService.list() }
    }

    @Test
    fun `GET vols by id returns vol`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()
        val vol = Vol(
            id = id,
            numeroVol = "AF456",
            origine = "ORY",
            destination = "NCE",
            heureDepart = now,
            heureArrivee = now.plusHours(1),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        every { volService.get(id) } returns Mono.just(vol)

        webTestClient.get()
            .uri("/vols/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.numeroVol").isEqualTo("AF456")
            .jsonPath("$.origine").isEqualTo("ORY")

        verify { volService.get(id) }
    }

    @Test
    fun `GET vols by id returns 404 when not found`() {
        val id = UUID.randomUUID()

        every { volService.get(id) } returns Mono.error(NotFoundException("Vol $id non trouvé"))

        webTestClient.get()
            .uri("/vols/$id")
            .exchange()
            .expectStatus().isNotFound

        verify { volService.get(id) }
    }

    @Test
    fun `DELETE vols removes vol`() {
        val id = UUID.randomUUID()

        every { volService.delete(id) } returns Mono.just(Unit)

        webTestClient.delete()
            .uri("/vols/$id")
            .exchange()
            .expectStatus().isOk

        verify { volService.delete(id) }
    }

    @Test
    fun `POST vols assign avion succeeds`() {
        val volId = UUID.randomUUID()
        val avionId = UUID.randomUUID()
        val now = LocalDateTime.now()
        val updated = Vol(
            id = volId,
            numeroVol = "AF222",
            origine = "CDG",
            destination = "BCN",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = avionId,
            pisteId = null
        )

        every { volService.assignAvion(volId, avionId) } returns Mono.just(updated)

        webTestClient.post()
            .uri("/vols/$volId/avion/$avionId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.avionId").isEqualTo(avionId.toString())

        verify { volService.assignAvion(volId, avionId) }
    }

    @Test
    fun `DELETE vols avion unassigns avion`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()
        val updated = Vol(
            id = id,
            numeroVol = "AF333",
            origine = "CDG",
            destination = "LIS",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        every { volService.unassignAvion(id) } returns Mono.just(updated)

        webTestClient.delete()
            .uri("/vols/$id/avion")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.avionId").isEmpty

        verify { volService.unassignAvion(id) }
    }

    @Test
    fun `PUT vols etat updates state`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()
        val updated = Vol(
            id = id,
            numeroVol = "AF444",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.EMBARQUEMENT,
            avionId = UUID.randomUUID(),
            pisteId = null
        )

        every { volService.updateEtat(id, VolEtat.EMBARQUEMENT) } returns Mono.just(updated)

        webTestClient.put()
            .uri("/vols/$id/etat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("etat" to "EMBARQUEMENT"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.etat").isEqualTo("EMBARQUEMENT")

        verify { volService.updateEtat(id, VolEtat.EMBARQUEMENT) }
    }

    @Test
    fun `GET vols by etat returns filtered list`() {
        val now = LocalDateTime.now()
        val vol = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF555",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.EN_VOL,
            avionId = UUID.randomUUID(),
            pisteId = null
        )

        every { volService.listByEtat(VolEtat.EN_VOL) } returns Flux.just(vol)

        webTestClient.get()
            .uri("/vols/etat/EN_VOL")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { volService.listByEtat(VolEtat.EN_VOL) }
    }

    @Test
    fun `POST vols assign piste throws UnsupportedOperationException`() {
        val volId = UUID.randomUUID()
        val pisteId = UUID.randomUUID()

        webTestClient.post()
            .uri("/vols/$volId/piste/$pisteId")
            .exchange()
            .expectStatus().is5xxServerError

        verify(exactly = 0) { volService.assignAvion(any(), any()) }
    }

    @Test
    fun `DELETE vols piste throws UnsupportedOperationException`() {
        val volId = UUID.randomUUID()

        webTestClient.delete()
            .uri("/vols/$volId/piste")
            .exchange()
            .expectStatus().is5xxServerError

        verify(exactly = 0) { volService.unassignAvion(any()) }
    }

    @Test
    fun `GET vols departures returns departures from airport`() {
        val now = LocalDateTime.now()
        val vol = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF666",
            origine = "CDG",
            destination = "NYC",
            heureDepart = now,
            heureArrivee = now.plusHours(8),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        every { volService.listDeparturesFrom("CDG") } returns Flux.just(vol)

        webTestClient.get()
            .uri("/vols/departures")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { volService.listDeparturesFrom("CDG") }
    }

    @Test
    fun `GET vols arrivals returns arrivals to airport`() {
        val now = LocalDateTime.now()
        val vol = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF777",
            origine = "NYC",
            destination = "CDG",
            heureDepart = now,
            heureArrivee = now.plusHours(8),
            etat = VolEtat.EN_VOL,
            avionId = UUID.randomUUID(),
            pisteId = null
        )

        every { volService.listArrivalsTo("CDG") } returns Flux.just(vol)

        webTestClient.get()
            .uri("/vols/arrivals")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { volService.listArrivalsTo("CDG") }
    }

    @Test
    fun `GET vols traffic returns all traffic for airport`() {
        val now = LocalDateTime.now()
        val vol1 = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF888",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        every { volService.trafficFor("CDG") } returns Flux.just(vol1)

        webTestClient.get()
            .uri("/vols/traffic")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { volService.trafficFor("CDG") }
    }

    @Test
    fun `GET vols departures to destination returns VolDto list`() {
        val now = LocalDateTime.now()
        val volDto = VolDto(
            id = UUID.randomUUID().toString(),
            numeroVol = "AF999",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            origine = "CDG",
            destination = "MAD",
            etat = "PREVU",
            avionImmatriculation = "F-GRNB",
            pisteId = null
        )

        every { volService.findArrivalsToDestination("MAD") } returns Flux.just(volDto)

        webTestClient.get()
            .uri("/vols/departures/MAD")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { volService.findArrivalsToDestination("MAD") }
    }
}
