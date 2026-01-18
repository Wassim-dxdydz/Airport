package fr.uga.miage.m1.app.controller

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import com.ninjasquad.springmockk.MockkBean
import fr.uga.miage.m1.domain.model.Piste
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.service.PisteService
import fr.uga.miage.m1.domain.service.VolService
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteRequest
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@WebFluxTest(PisteController::class)
class PisteControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var pisteService: PisteService

    @MockkBean
    private lateinit var volService: VolService

    @Test
    fun `GET pistes returns list`() {
        val piste = Piste(UUID.randomUUID(), "R1", 3000, PisteEtat.LIBRE)

        every { pisteService.list() } returns Flux.just(piste)

        webTestClient.get()
            .uri("/pistes")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { pisteService.list() }
    }

    @Test
    fun `GET pistes by id returns piste`() {
        val id = UUID.randomUUID()
        val piste = Piste(id, "R1", 3000, PisteEtat.LIBRE)

        every { pisteService.get(id) } returns Mono.just(piste)

        webTestClient.get()
            .uri("/pistes/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.identifiant").isEqualTo("R1")
            .jsonPath("$.longueurM").isEqualTo(3000)
            .jsonPath("$.etat").isEqualTo("LIBRE")

        verify { pisteService.get(id) }
    }

    @Test
    fun `GET pistes by id returns 404 when not found`() {
        val id = UUID.randomUUID()

        every { pisteService.get(id) } returns Mono.error(NotFoundException("Piste $id non trouvée"))

        webTestClient.get()
            .uri("/pistes/$id")
            .exchange()
            .expectStatus().isNotFound

        verify { pisteService.get(id) }
    }

    @Test
    fun `POST pistes creates new piste`() {
        val request = CreatePisteRequest(
            identifiant = "R2",
            longueurM = 2500,
            etat = PisteEtat.LIBRE
        )
        val created = Piste(UUID.randomUUID(), "R2", 2500, PisteEtat.LIBRE)

        every { pisteService.create(any()) } returns Mono.just(created)

        webTestClient.post()
            .uri("/pistes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.identifiant").isEqualTo("R2")
            .jsonPath("$.longueurM").isEqualTo(2500)

        verify { pisteService.create(any()) }
    }

    @Test
    fun `PATCH pistes updates existing piste`() {
        val id = UUID.randomUUID()
        val current = Piste(id, "R1", 3000, PisteEtat.LIBRE)
        val request = UpdatePisteRequest(
            identifiant = null,
            longueurM = null,
            etat = PisteEtat.OCCUPEE
        )
        val updated = current.copy(etat = PisteEtat.OCCUPEE)

        every { pisteService.get(id) } returns Mono.just(current)
        every { pisteService.update(id, any()) } returns Mono.just(updated)

        webTestClient.patch()
            .uri("/pistes/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.etat").isEqualTo("OCCUPEE")

        verify { pisteService.get(id) }
        verify { pisteService.update(id, any()) }
    }

    @Test
    fun `DELETE pistes removes piste`() {
        val id = UUID.randomUUID()

        every { pisteService.delete(id) } returns Mono.just(Unit)

        webTestClient.delete()
            .uri("/pistes/$id")
            .exchange()
            .expectStatus().isOk

        verify { pisteService.delete(id) }
    }

    @Test
    fun `GET pistes disponibles returns only LIBRE pistes`() {
        val piste = Piste(UUID.randomUUID(), "R1", 3000, PisteEtat.LIBRE)

        every { pisteService.disponibles() } returns Flux.just(piste)

        webTestClient.get()
            .uri("/pistes/disponibles")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { pisteService.disponibles() }
    }

    @Test
    fun `GET pistes planning returns vols for piste`() {
        val pisteId = UUID.randomUUID()
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
            pisteId = pisteId
        )

        every { volService.listByPiste(pisteId) } returns Flux.just(vol)

        webTestClient.get()
            .uri("/pistes/$pisteId/planning")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { volService.listByPiste(pisteId) }
    }
}
