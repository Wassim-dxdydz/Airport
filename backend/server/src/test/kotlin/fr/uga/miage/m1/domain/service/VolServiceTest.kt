package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.model.VolHistory
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.PisteDataPort
import fr.uga.miage.m1.domain.port.VolDataPort
import fr.uga.miage.m1.exceptions.NotFoundException
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.UUID

class VolServiceTest {

    private lateinit var volPort: VolDataPort
    private lateinit var avionPort: AvionDataPort
    private lateinit var pistePort: PisteDataPort
    private lateinit var volHistoryService: VolHistoryService
    private lateinit var pisteService: PisteService

    private lateinit var service: VolService

    @BeforeEach
    fun setup() {
        volPort = mockk()
        avionPort = mockk()
        pistePort = mockk()
        volHistoryService = mockk()
        pisteService = mockk()

        every { volHistoryService.save(any()) } returns Mono.just(
            VolHistory(id = UUID.randomUUID(), volId = UUID.randomUUID(), etat = VolEtat.PREVU)
        )

        service = VolService(
            volPort,
            avionPort,
            volHistoryService,
            pistePort,
            pisteService,
            myAirportCode = "ATL"
        )
    }

    @Test
    fun `list returns vols`() {
        val v = Vol(UUID.randomUUID(), "AF1000", "CDG", "MAD",
            LocalDateTime.now(), LocalDateTime.now().plusHours(2),
            VolEtat.PREVU, null, null)

        every { volPort.findAll() } returns Flux.just(v)

        StepVerifier.create(service.list())
            .expectNext(v)
            .verifyComplete()
    }

    @Test
    fun `get returns vol`() {
        val id = UUID.randomUUID()
        val v = Vol(id, "AF1001", "CDG", "NYC",
            LocalDateTime.now(), LocalDateTime.now().plusHours(7),
            VolEtat.PREVU, null, null)

        every { volPort.findById(id) } returns Mono.just(v)

        StepVerifier.create(service.get(id))
            .expectNext(v)
            .verifyComplete()
    }

    @Test
    fun `get throws NotFoundException when missing`() {
        val id = UUID.randomUUID()
        every { volPort.findById(id) } returns Mono.empty()

        StepVerifier.create(service.get(id))
            .expectError(NotFoundException::class.java)
            .verify()
    }

    @Test
    fun `create sets etat PREVU and saves`() {
        val now = LocalDateTime.now()
        val vol = Vol(null, "AF2000", "CDG", "LHR",
            now.plusHours(1), now.plusHours(2),
            VolEtat.PREVU, null, null)

        val saved = vol.copy(id = UUID.randomUUID())

        every { volPort.save(any()) } returns Mono.just(saved)

        StepVerifier.create(service.create(vol))
            .expectNext(saved)
            .verifyComplete()

        verify { volPort.save(any()) }
        verify { volHistoryService.save(any()) }
    }

    @Test
    fun `update merges fields and saves`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        val existing = Vol(id, "AF3000", "LYS", "CDG",
            now.plusHours(1), now.plusHours(3),
            VolEtat.PREVU, null, null)

        val updated = existing.copy(origine = "MRS")

        every { volPort.findById(id) } returns Mono.just(existing)
        every { volPort.save(any()) } returns Mono.just(updated)

        StepVerifier.create(service.update(id, updated))
            .expectNext(updated)
            .verifyComplete()

        verify { volPort.save(match { it.origine == "MRS" }) }
        verify { volHistoryService.save(any()) }
    }

    @Test
    fun `updateBasicFields updates selected fields`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        val existing = Vol(id, "AF3010", "CDG", "LHR",
            now, now.plusHours(2), VolEtat.PREVU, null, null)

        val patched = existing.copy(origine = "ALX")

        every { volPort.findById(id) } returns Mono.just(existing)
        every { volPort.save(any()) } returns Mono.just(patched)

        StepVerifier.create(service.updateBasicFields(id, patched))
            .expectNext(patched)
            .verifyComplete()
    }

    @Test
    fun `assignAvion fails if avion not found`() {
        val volId = UUID.randomUUID()
        val avionId = UUID.randomUUID()

        every { volPort.findById(volId) } returns Mono.just(mockk())
        every { avionPort.findById(avionId) } returns Mono.empty()

        StepVerifier.create(service.assignAvion(volId, avionId))
            .expectError(NotFoundException::class.java)
            .verify()
    }

    @Test
    fun `assignAvion succeeds`() {
        val volId = UUID.randomUUID()
        val avionId = UUID.randomUUID()
        val hangarId = UUID.randomUUID()
        val now = LocalDateTime.now()

        val current = Vol(volId, "AF4000", "CDG", "MAD",
            now.plusHours(1), now.plusHours(3), VolEtat.PREVU, null, null)

        val avion = Avion(
            id = avionId,
            immatriculation = "F-ABCD",
            type = "A320",
            capacite = 150,
            etat = AvionEtat.DISPONIBLE,
            hangarId = hangarId
        )

        val updated = current.copy(avionId = avionId)

        every { avionPort.findById(avionId) } returns Mono.just(avion)
        every { volPort.findById(volId) } returns Mono.just(current)
        every { volPort.save(any()) } returns Mono.just(updated)

        StepVerifier.create(service.assignAvion(volId, avionId))
            .expectNextMatches { it.avionId == avionId }
            .verifyComplete()
    }

    @Test
    fun `unassignAvion sets avionId to null`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        val current = Vol(id, "AF5000", "MAD", "CDG",
            now.plusHours(1), now.plusHours(3), VolEtat.PREVU, UUID.randomUUID(), null)

        val updated = current.copy(avionId = null)

        every { volPort.findById(id) } returns Mono.just(current)
        every { volPort.save(any()) } returns Mono.just(updated)

        StepVerifier.create(service.unassignAvion(id))
            .expectNextMatches { it.avionId == null }
            .verifyComplete()
    }

    @Test
    fun `updateEtat saves when transition allowed`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()
        val current = Vol(id, "AF6000", "CDG", "LIS",
            now.plusHours(1), now.plusHours(3),
            VolEtat.PREVU, UUID.randomUUID(), null)

        val target = VolEtat.EN_ATTENTE
        val updated = current.copy(etat = target)

        every { volPort.findById(id) } returns Mono.just(current)
        every { volPort.save(any()) } returns Mono.just(updated)

        StepVerifier.create(service.updateEtat(id, target))
            .expectNextMatches { it.etat == target }
            .verifyComplete()
    }

    @Test
    fun `updateEtat fails when transition forbidden`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        val current = Vol(id, "AF6001", "LIS", "CDG",
            now.plusHours(1), now.plusHours(3),
            VolEtat.ARRIVE, UUID.randomUUID(), null)

        val target = VolEtat.PREVU

        every { volPort.findById(id) } returns Mono.just(current)

        StepVerifier.create(service.updateEtat(id, target))
            .expectError(IllegalStateException::class.java)
            .verify()

        verify(exactly = 0) { volPort.save(any()) }
    }

    @Test
    fun `listByEtat delegates to port`() {
        val now = LocalDateTime.now()

        val v1 = Vol(
            UUID.randomUUID(), "AF9000", "CDG", "MAD",
            now.plusHours(2), now.plusHours(4), VolEtat.EN_VOL, null, null
        )

        every { volPort.findByEtat(VolEtat.EN_VOL) } returns Flux.just(v1)

        StepVerifier.create(service.listByEtat(VolEtat.EN_VOL))
            .expectNext(v1)
            .verifyComplete()

        verify { volPort.findByEtat(VolEtat.EN_VOL) }
    }

    @Test
    fun `listDeparturesFrom delegates`() {
        val airport = "ALG"
        val v = Vol(UUID.randomUUID(), "AF9001", airport, "PAR",
            LocalDateTime.now(), LocalDateTime.now().plusHours(2),
            VolEtat.PREVU, null, null)

        every { volPort.findByOrigine(airport) } returns Flux.just(v)

        StepVerifier.create(service.listDeparturesFrom(airport))
            .expectNext(v)
            .verifyComplete()
    }

    @Test
    fun `listArrivalsTo delegates`() {
        val airport = "LYS"
        val v = Vol(UUID.randomUUID(), "AF9002", "PAR", airport,
            LocalDateTime.now(), LocalDateTime.now().plusHours(2),
            VolEtat.PREVU, null, null)

        every { volPort.findByDestination(airport) } returns Flux.just(v)

        StepVerifier.create(service.listArrivalsTo(airport))
            .expectNext(v)
            .verifyComplete()
    }

    @Test
    fun `trafficFor merges arrival and departure streams`() {
        val airport = "ALG"

        val v1 = Vol(UUID.randomUUID(), "AF9100", airport, "TUN",
            LocalDateTime.now(), LocalDateTime.now().plusHours(1),
            VolEtat.PREVU, null, null)

        val v2 = Vol(UUID.randomUUID(), "AF9101", "LYS", airport,
            LocalDateTime.now(), LocalDateTime.now().plusHours(1),
            VolEtat.PREVU, null, null)

        every { volPort.findByOrigine(airport) } returns Flux.just(v1)
        every { volPort.findByDestination(airport) } returns Flux.just(v2)

        StepVerifier.create(service.trafficFor(airport))
            .expectNext(v1, v2)
            .verifyComplete()
    }
}
