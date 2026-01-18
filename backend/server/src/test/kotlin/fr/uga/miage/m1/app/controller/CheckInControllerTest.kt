package fr.uga.miage.m1.app.controller

import com.ninjasquad.springmockk.MockkBean
import fr.uga.miage.m1.domain.model.CheckIn
import fr.uga.miage.m1.domain.model.Passenger
import fr.uga.miage.m1.domain.port.PassengerDataPort
import fr.uga.miage.m1.domain.service.CheckInService
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.requests.CreateCheckInRequest
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

@WebFluxTest(CheckInController::class)
class CheckInControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var checkInService: CheckInService

    @MockkBean
    private lateinit var passengerPort: PassengerDataPort

    @Test
    fun `GET check-ins returns list with passenger info`() {
        val passagerId = UUID.randomUUID()
        val checkIn = CheckIn(
            id = UUID.randomUUID(),
            volId = UUID.randomUUID(),
            passagerId = passagerId,
            numeroSiege = "12A",
            heureCheckIn = LocalDateTime.now()
        )
        val passenger = Passenger(
            id = passagerId,
            nom = "Dupont",
            prenom = "Jean",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )

        every { checkInService.list() } returns Flux.just(checkIn)
        every { passengerPort.findById(passagerId) } returns Mono.just(passenger)

        webTestClient.get()
            .uri("/check-ins")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { checkInService.list() }
        verify { passengerPort.findById(passagerId) }
    }

    @Test
    fun `GET check-ins by id returns check-in with passenger info`() {
        val id = UUID.randomUUID()
        val passagerId = UUID.randomUUID()
        val checkIn = CheckIn(
            id = id,
            volId = UUID.randomUUID(),
            passagerId = passagerId,
            numeroSiege = "15C",
            heureCheckIn = LocalDateTime.now()
        )
        val passenger = Passenger(
            id = passagerId,
            nom = "Martin",
            prenom = "Sophie",
            email = "sophie.martin@example.com",
            telephone = "+33687654321"
        )

        every { checkInService.get(id) } returns Mono.just(checkIn)
        every { passengerPort.findById(passagerId) } returns Mono.just(passenger)

        webTestClient.get()
            .uri("/check-ins/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(id.toString())
            .jsonPath("$.numeroSiege").isEqualTo("15C")
            .jsonPath("$.passagerInfo.nom").isEqualTo("Martin")

        verify { checkInService.get(id) }
        verify { passengerPort.findById(passagerId) }
    }

    @Test
    fun `GET check-ins by id returns 404 when not found`() {
        val id = UUID.randomUUID()

        every { checkInService.get(id) } returns Mono.error(NotFoundException("Check-in $id non trouvé"))

        webTestClient.get()
            .uri("/check-ins/$id")
            .exchange()
            .expectStatus().isNotFound

        verify { checkInService.get(id) }
    }

    @Test
    fun `POST check-ins creates new check-in`() {
        val passagerId = UUID.randomUUID()
        val volId = UUID.randomUUID()
        val request = CreateCheckInRequest(
            passagerId = passagerId,
            volId = volId,
            numeroSiege = "20A"
        )
        val created = CheckIn(
            id = UUID.randomUUID(),
            volId = volId,
            passagerId = passagerId,
            numeroSiege = "20A",
            heureCheckIn = LocalDateTime.now()
        )
        val passenger = Passenger(
            id = passagerId,
            nom = "Lefebvre",
            prenom = "Pierre",
            email = "pierre.lefebvre@example.com",
            telephone = "+33698765432"
        )

        every { checkInService.create(any()) } returns Mono.just(created)
        every { passengerPort.findById(passagerId) } returns Mono.just(passenger)

        webTestClient.post()
            .uri("/check-ins")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.numeroSiege").isEqualTo("20A")
            .jsonPath("$.passagerInfo.nom").isEqualTo("Lefebvre")

        verify { checkInService.create(any()) }
        verify { passengerPort.findById(passagerId) }
    }

    @Test
    fun `GET check-ins by vol returns filtered list`() {
        val volId = UUID.randomUUID()
        val passagerId = UUID.randomUUID()
        val checkIn = CheckIn(
            id = UUID.randomUUID(),
            volId = volId,
            passagerId = passagerId,
            numeroSiege = "10B",
            heureCheckIn = LocalDateTime.now()
        )
        val passenger = Passenger(
            id = passagerId,
            nom = "Dubois",
            prenom = "Marie",
            email = "marie.dubois@example.com",
            telephone = null
        )

        every { checkInService.listByVol(volId) } returns Flux.just(checkIn)
        every { passengerPort.findById(passagerId) } returns Mono.just(passenger)

        webTestClient.get()
            .uri("/check-ins/vol/$volId")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Any::class.java)
            .hasSize(1)

        verify { checkInService.listByVol(volId) }
        verify { passengerPort.findById(passagerId) }
    }

    @Test
    fun `GET verify passenger check-in returns boolean`() {
        val volId = UUID.randomUUID()
        val passagerId = UUID.randomUUID()

        every { checkInService.verifyPassengerCheckIn(volId, passagerId) } returns Mono.just(true)

        webTestClient.get()
            .uri("/check-ins/vol/$volId/passenger/$passagerId/verify")
            .exchange()
            .expectStatus().isOk
            .expectBody(Boolean::class.java)
            .isEqualTo(true)

        verify { checkInService.verifyPassengerCheckIn(volId, passagerId) }
    }
}
