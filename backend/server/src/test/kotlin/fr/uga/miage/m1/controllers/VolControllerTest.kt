package fr.uga.miage.m1.controllers

import com.ninjasquad.springmockk.MockkBean
import fr.uga.miage.m1.models.Vol
import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.requests.UpdateVolRequest
import fr.uga.miage.m1.services.VolService
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@WebFluxTest(controllers = [VolController::class])
class VolControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @MockkBean
    lateinit var service: VolService

    @Test
    fun `GET list returns 200 with payload`() {
        val now = LocalDateTime.now()
        val v1 = Vol(
            numeroVol = "AF1234",
            origine = "CDG",
            destination = "JFK",
            heureDepart = now.plusHours(2),
            heureArrivee = now.plusHours(8),
            etat = VolEtat.PREVU
        )
        every { service.list() } returns Flux.just(v1)

        client.get().uri("/api/vols")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].numeroVol").isEqualTo("AF1234")
            .jsonPath("$[0].origine").isEqualTo("CDG")
            .jsonPath("$[0].destination").isEqualTo("JFK")
            .jsonPath("$[0].etat").isEqualTo("PREVU")
    }

    @Test
    fun `POST create returns 201`() {
        val now = LocalDateTime.now()
        val saved = Vol(
            numeroVol = "AF2222",
            origine = "LYS",
            destination = "ORY",
            heureDepart = now.plusHours(3),
            heureArrivee = now.plusHours(5),
            etat = VolEtat.PREVU
        )
        every { service.create(any<CreateVolRequest>()) } returns Mono.just(saved)

        client.post().uri("/api/vols")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "numeroVol": "AF2222",
                  "origine": "LYS",
                  "destination": "ORY",
                  "heureDepart": "${now.plusHours(3)}",
                  "heureArrivee": "${now.plusHours(5)}"
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.numeroVol").isEqualTo("AF2222")
            .jsonPath("$.etat").isEqualTo("PREVU")
    }

    @Test
    fun `GET by id returns 200 with payload`() {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()
        val vol = Vol(
            numeroVol = "AF3000",
            origine = "NCE",
            destination = "CDG",
            heureDepart = now.plusHours(4),
            heureArrivee = now.plusHours(6),
            etat = VolEtat.EN_ATTENTE
        )
        every { service.get(id) } returns Mono.just(vol)

        client.get().uri("/api/vols/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.numeroVol").isEqualTo("AF3000")
            .jsonPath("$.etat").isEqualTo("EN_ATTENTE")
    }

    @Test
    fun `PUT update returns 200 with updated payload`() {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()
        val updated = Vol(
            numeroVol = "AF4000",
            origine = "MRS",
            destination = "CDG",
            heureDepart = now.plusHours(2),
            heureArrivee = now.plusHours(4),
            etat = VolEtat.EMBARQUEMENT
        )
        every { service.update(eq(id), any<UpdateVolRequest>()) } returns Mono.just(updated)

        client.put().uri("/api/vols/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "origine": "MRS",
                  "destination": "CDG",
                  "heureDepart": "${now.plusHours(2)}",
                  "heureArrivee": "${now.plusHours(4)}",
                  "etat": "EMBARQUEMENT",
                  "avionId": null
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.origine").isEqualTo("MRS")
            .jsonPath("$.etat").isEqualTo("EMBARQUEMENT")
    }

    @Test
    fun `DELETE returns 204`() {
        val id = UUID.randomUUID()
        every { service.delete(id) } returns Mono.empty()

        client.delete().uri("/api/vols/$id")
            .exchange()
            .expectStatus().isNoContent
            .expectBody().isEmpty
    }

    @Test
    fun `POST assignAvion returns 200 with avionId set`() {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()
        val avionId = UUID.randomUUID()
        val withAvion = Vol(
            numeroVol = "AF5000",
            origine = "LYS",
            destination = "LHR",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(3),
            etat = VolEtat.PREVU,
            avionId = avionId
        )
        every { service.assignAvion(id, avionId) } returns Mono.just(withAvion)

        client.post().uri("/api/vols/$id/assign-avion/$avionId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.numeroVol").isEqualTo("AF5000")
            .jsonPath("$.avionId").isEqualTo(avionId.toString())
    }

    @Test
    fun `POST unassignAvion returns 200 with avionId cleared`() {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()
        val withoutAvion = Vol(
            numeroVol = "AF5001",
            origine = "LHR",
            destination = "LYS",
            heureDepart = now.plusHours(2),
            heureArrivee = now.plusHours(4),
            etat = VolEtat.PREVU,
            avionId = null
        )
        every { service.unassignAvion(id) } returns Mono.just(withoutAvion)

        client.post().uri("/api/vols/$id/unassign-avion")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.numeroVol").isEqualTo("AF5001")
            .jsonPath("$.avionId").doesNotExist()
    }

    @Test
    fun `PATCH updateEtat returns 200`() {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()
        val updated = Vol(
            numeroVol = "AF6000",
            origine = "CDG",
            destination = "FCO",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(3),
            etat = VolEtat.DECOLLE
        )
        every { service.updateEtat(id, VolEtat.DECOLLE) } returns Mono.just(updated)

        // @RequestBody etat: VolEtat -> send JSON string literal
        client.patch().uri("/api/vols/$id/etat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("\"DECOLLE\"")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.etat").isEqualTo("DECOLLE")
    }

    @Test
    fun `GET listByEtat returns 200 with filtered vols`() {
        val now = LocalDateTime.now()
        val v1 = Vol(
            numeroVol = "AF7000",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now.plusHours(2),
            heureArrivee = now.plusHours(4),
            etat = VolEtat.EN_VOL
        )
        val v2 = Vol(
            numeroVol = "AF7001",
            origine = "MAD",
            destination = "CDG",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(3),
            etat = VolEtat.EN_VOL
        )
        every { service.listByEtat(VolEtat.EN_VOL) } returns Flux.just(v1, v2)

        client.get().uri("/api/vols/etat/EN_VOL")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].etat").isEqualTo("EN_VOL")
            .jsonPath("$[1].etat").isEqualTo("EN_VOL")
    }
}
