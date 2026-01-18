package fr.uga.miage.m1.domain.service

import fr.uga.miage.m1.domain.model.CheckIn
import fr.uga.miage.m1.domain.model.Passenger
import fr.uga.miage.m1.domain.port.CheckInDataPort
import fr.uga.miage.m1.domain.port.PassengerDataPort
import fr.uga.miage.m1.exceptions.NotFoundException
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.UUID

class PassengerServiceTest {

    private lateinit var passengerPort: PassengerDataPort
    private lateinit var checkInPort: CheckInDataPort
    private lateinit var service: PassengerService

    @BeforeEach
    fun setup() {
        passengerPort = mockk()
        checkInPort = mockk()
        service = PassengerService(passengerPort, checkInPort)
    }

    @Test
    fun `list returns all passengers`() {
        val passenger = Passenger(
            id = UUID.randomUUID(),
            nom = "Dupont",
            prenom = "Jean",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )

        every { passengerPort.findAll() } returns Flux.just(passenger)

        StepVerifier.create(service.list())
            .expectNext(passenger)
            .verifyComplete()
    }

    @Test
    fun `get returns passenger when exists`() {
        val id = UUID.randomUUID()
        val passenger = Passenger(
            id = id,
            nom = "Martin",
            prenom = "Sophie",
            email = "sophie.martin@example.com",
            telephone = "+33687654321"
        )

        every { passengerPort.findById(id) } returns Mono.just(passenger)

        StepVerifier.create(service.get(id))
            .expectNext(passenger)
            .verifyComplete()
    }

    @Test
    fun `get throws NotFoundException when not found`() {
        val id = UUID.randomUUID()

        every { passengerPort.findById(id) } returns Mono.empty()

        StepVerifier.create(service.get(id))
            .expectError(NotFoundException::class.java)
            .verify()
    }

    @Test
    fun `create succeeds when email is unique`() {
        val passenger = Passenger(
            id = null,
            nom = "Lefebvre",
            prenom = "Pierre",
            email = "pierre.lefebvre@example.com",
            telephone = "+33698765432"
        )
        val saved = passenger.copy(id = UUID.randomUUID())

        every { passengerPort.existsByEmail(passenger.email) } returns Mono.just(false)
        every { passengerPort.save(passenger) } returns Mono.just(saved)

        StepVerifier.create(service.create(passenger))
            .expectNext(saved)
            .verifyComplete()
    }

    @Test
    fun `create throws when email already exists`() {
        val passenger = Passenger(
            id = null,
            nom = "Dupont",
            prenom = "Jean",
            email = "existing@example.com",
            telephone = "+33612345678"
        )

        every { passengerPort.existsByEmail(passenger.email) } returns Mono.just(true)

        StepVerifier.create(service.create(passenger))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("Un passager avec cet email existe déjà")
            }
            .verify()
    }

    @Test
    fun `update succeeds when email unchanged`() {
        val id = UUID.randomUUID()
        val current = Passenger(
            id = id,
            nom = "Dupont",
            prenom = "Jean",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )
        val updated = Passenger(
            id = null,
            nom = "Dupont",
            prenom = "Jean-Pierre",
            email = "jean.dupont@example.com",
            telephone = "+33699999999"
        )
        val merged = current.copy(prenom = "Jean-Pierre", telephone = "+33699999999")

        every { passengerPort.findById(id) } returns Mono.just(current)
        every { passengerPort.save(merged) } returns Mono.just(merged)

        StepVerifier.create(service.update(id, updated))
            .expectNextMatches { it.prenom == "Jean-Pierre" && it.telephone == "+33699999999" }
            .verifyComplete()
    }

    @Test
    fun `update succeeds when email changed to unique one`() {
        val id = UUID.randomUUID()
        val current = Passenger(
            id = id,
            nom = "Dupont",
            prenom = "Jean",
            email = "old@example.com",
            telephone = "+33612345678"
        )
        val updated = Passenger(
            id = null,
            nom = "Dupont",
            prenom = "Jean",
            email = "new@example.com",
            telephone = "+33612345678"
        )
        val merged = current.copy(email = "new@example.com")

        every { passengerPort.findById(id) } returns Mono.just(current)
        every { passengerPort.existsByEmail("new@example.com") } returns Mono.just(false)
        every { passengerPort.save(merged) } returns Mono.just(merged)

        StepVerifier.create(service.update(id, updated))
            .expectNextMatches { it.email == "new@example.com" }
            .verifyComplete()
    }

    @Test
    fun `update throws when email changed to existing one`() {
        val id = UUID.randomUUID()
        val current = Passenger(
            id = id,
            nom = "Dupont",
            prenom = "Jean",
            email = "old@example.com",
            telephone = "+33612345678"
        )
        val updated = Passenger(
            id = null,
            nom = "Dupont",
            prenom = "Jean",
            email = "existing@example.com",
            telephone = "+33612345678"
        )

        every { passengerPort.findById(id) } returns Mono.just(current)
        every { passengerPort.existsByEmail("existing@example.com") } returns Mono.just(true)

        StepVerifier.create(service.update(id, updated))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("Un passager avec cet email existe déjà")
            }
            .verify()
    }

    @Test
    fun `delete succeeds when passenger has no check-ins`() {
        val id = UUID.randomUUID()
        val passenger = Passenger(
            id = id,
            nom = "Dupont",
            prenom = "Jean",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )

        every { passengerPort.findById(id) } returns Mono.just(passenger)
        every { checkInPort.findByPassagerId(id) } returns Flux.empty()
        every { passengerPort.deleteById(id) } returns Mono.empty()

        StepVerifier.create(service.delete(id))
            .expectNext(Unit)
            .verifyComplete()

        verify { passengerPort.deleteById(id) }
    }

    @Test
    fun `delete throws when passenger has check-ins`() {
        val id = UUID.randomUUID()
        val passenger = Passenger(
            id = id,
            nom = "Dupont",
            prenom = "Jean",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )
        val checkIn = CheckIn(
            id = UUID.randomUUID(),
            volId = UUID.randomUUID(),
            passagerId = id,
            numeroSiege = "12A",
            heureCheckIn = LocalDateTime.now()
        )

        every { passengerPort.findById(id) } returns Mono.just(passenger)
        every { checkInPort.findByPassagerId(id) } returns Flux.just(checkIn)

        StepVerifier.create(service.delete(id))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("il a des enregistrements de check-in")
            }
            .verify()

        verify(exactly = 0) { passengerPort.deleteById(id) }
    }

    @Test
    fun `listNotCheckedInForVol returns only passengers not checked in`() {
        val volId = UUID.randomUUID()
        val p1 = Passenger(
            id = UUID.randomUUID(),
            nom = "Dupont",
            prenom = "Jean",
            email = "jean@example.com",
            telephone = "+33612345678"
        )
        val p2 = Passenger(
            id = UUID.randomUUID(),
            nom = "Martin",
            prenom = "Sophie",
            email = "sophie@example.com",
            telephone = "+33687654321"
        )

        every { passengerPort.findAll() } returns Flux.just(p1, p2)
        every { checkInPort.existsByVolIdAndPassagerId(volId, p1.id!!) } returns Mono.just(true)
        every { checkInPort.existsByVolIdAndPassagerId(volId, p2.id!!) } returns Mono.just(false)

        StepVerifier.create(service.listNotCheckedInForVol(volId))
            .expectNext(p2)
            .verifyComplete()
    }
}
