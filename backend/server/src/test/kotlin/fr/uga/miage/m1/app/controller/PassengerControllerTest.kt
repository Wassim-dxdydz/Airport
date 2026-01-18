package fr.uga.miage.m1.app.controller

import com.ninjasquad.springmockk.MockkBean
import fr.uga.miage.m1.domain.model.Passenger
import fr.uga.miage.m1.domain.service.PassengerService
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.requests.CreatePassengerRequest
import fr.uga.miage.m1.requests.UpdatePassengerRequest
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

@WebFluxTest(PassengerController::class)
class PassengerControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var passengerService: PassengerService

    @Test
    fun `GET passengers returns list`() {
        val passenger = Passenger(
            id = UUID.randomUUID(),
            nom = "Dupont",
            prenom = "Jean",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )

        every { passengerService.list() } returns Flux.just(passenger)

        webTestClient.get()
            .uri("/passengers")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { passengerService.list() }
    }

    @Test
    fun `GET passengers by id returns passenger`() {
        val id = UUID.randomUUID()
        val passenger = Passenger(
            id = id,
            nom = "Martin",
            prenom = "Sophie",
            email = "sophie.martin@example.com",
            telephone = "+33687654321"
        )

        every { passengerService.get(id) } returns Mono.just(passenger)

        webTestClient.get()
            .uri("/passengers/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.nom").isEqualTo("Martin")
            .jsonPath("$.prenom").isEqualTo("Sophie")
            .jsonPath("$.email").isEqualTo("sophie.martin@example.com")

        verify { passengerService.get(id) }
    }

    @Test
    fun `GET passengers by id returns 404 when not found`() {
        val id = UUID.randomUUID()

        every { passengerService.get(id) } returns Mono.error(NotFoundException("Passager $id non trouvé"))

        webTestClient.get()
            .uri("/passengers/$id")
            .exchange()
            .expectStatus().isNotFound

        verify { passengerService.get(id) }
    }

    @Test
    fun `POST passengers creates new passenger`() {
        val request = CreatePassengerRequest(
            prenom = "Pierre",
            nom = "Lefebvre",
            email = "pierre.lefebvre@example.com",
            telephone = "+33698765432"
        )
        val created = Passenger(
            id = UUID.randomUUID(),
            prenom = "Pierre",
            nom = "Lefebvre",
            email = "pierre.lefebvre@example.com",
            telephone = "+33698765432"
        )

        every { passengerService.create(any()) } returns Mono.just(created)

        webTestClient.post()
            .uri("/passengers")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.nom").isEqualTo("Lefebvre")
            .jsonPath("$.prenom").isEqualTo("Pierre")

        verify { passengerService.create(any()) }
    }

    @Test
    fun `PATCH passengers updates existing passenger`() {
        val id = UUID.randomUUID()
        val current = Passenger(
            id = id,
            prenom = "Jean",
            nom = "Dupont",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )
        val request = UpdatePassengerRequest(
            prenom = "Jean-Pierre",
            nom = null,
            email = null,
            telephone = "+33699999999"
        )
        val updated = current.copy(prenom = "Jean-Pierre", telephone = "+33699999999")

        every { passengerService.get(id) } returns Mono.just(current)
        every { passengerService.update(id, any()) } returns Mono.just(updated)

        webTestClient.patch()
            .uri("/passengers/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.prenom").isEqualTo("Jean-Pierre")
            .jsonPath("$.telephone").isEqualTo("+33699999999")

        verify { passengerService.get(id) }
        verify { passengerService.update(id, any()) }
    }

    @Test
    fun `DELETE passengers removes passenger`() {
        val id = UUID.randomUUID()

        every { passengerService.delete(id) } returns Mono.just(Unit)

        webTestClient.delete()
            .uri("/passengers/$id")
            .exchange()
            .expectStatus().isOk

        verify { passengerService.delete(id) }
    }

    @Test
    fun `GET passengers not checked in for vol returns filtered list`() {
        val volId = UUID.randomUUID()
        val passenger = Passenger(
            id = UUID.randomUUID(),
            nom = "Dubois",
            prenom = "Marie",
            email = "marie.dubois@example.com",
            telephone = null
        )

        every { passengerService.listNotCheckedInForVol(volId) } returns Flux.just(passenger)

        webTestClient.get()
            .uri("/passengers/not-checked-in/vol/$volId")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { passengerService.listNotCheckedInForVol(volId) }
    }
}
