package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.model.VolHistory
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.PisteDataPort
import fr.uga.miage.m1.domain.port.VolDataPort
import fr.uga.miage.m1.domain.port.VolHistoryDataPort
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
    private lateinit var strategy: VolStatusStrategy
    private lateinit var pistePort: PisteDataPort
    private lateinit var volHistoryPort: VolHistoryDataPort
    private lateinit var syncService: SharedVolSyncService

    private lateinit var service: VolService

    @BeforeEach
    fun setup() {
        volPort = mockk()
        avionPort = mockk()
        strategy = mockk()
        pistePort = mockk()
        volHistoryPort = mockk()
        syncService = mockk()

        // THE IMPORTANT FIX
        every { volHistoryPort.save(any()) } returns Mono.just(
            VolHistory(
                id = UUID.randomUUID(),
                volId = UUID.randomUUID(),
                etat = VolEtat.PREVU
            )
        )

        service = VolService(
            volPort,
            avionPort,
            syncService,
            volHistoryPort,
            strategy,
            pistePort
        )
    }

    @Test
    fun `list returns vols`() {
        val v = Vol(
            UUID.randomUUID(), "AF1000", "CDG", "MAD",
            LocalDateTime.now(), LocalDateTime.now().plusHours(2),
            VolEtat.PREVU, null, null
        )

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
    fun `create sets etat PREVU, saves and pushes to partner`() {

        val now = LocalDateTime.now()
        val vol = Vol(
            id = null,
            numeroVol = "AF2000",
            origine = "CDG",
            destination = "LHR",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        val saved = vol.copy(id = UUID.randomUUID())

        every { volPort.save(any()) } returns Mono.just(saved)
        every { syncService.pushToPartner(saved) } returns Mono.empty()

        StepVerifier.create(service.create(vol))
            .expectNext(saved)
            .verifyComplete()

        verify { volPort.save(match { it.numeroVol == "AF2000" }) }
        verify { syncService.pushToPartner(saved) }
        verify { volHistoryPort.save(any()) }
    }

    @Test
    fun `update merges fields, saves and pushes to partner`() {

        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        val existing = Vol(
            id, "AF3000", "LYS", "CDG",
            now.plusHours(1), now.plusHours(3),
            VolEtat.PREVU, null, null
        )

        val updated = existing.copy(origine = "MRS")

        every { volPort.findById(id) } returns Mono.just(existing)
        every { volPort.save(any()) } returns Mono.just(updated)
        every { syncService.pushToPartner(updated) } returns Mono.empty()

        StepVerifier.create(service.update(id, updated))
            .expectNext(updated)
            .verifyComplete()

        verify { volPort.save(match { it.origine == "MRS" }) }
        verify { syncService.pushToPartner(updated) }
        verify { volHistoryPort.save(any()) }
    }


    @Test
    fun `assignAvion fails if avion not found`() {
        val volId = UUID.randomUUID()
        val avionId = UUID.randomUUID()

        every { avionPort.findById(avionId) } returns Mono.empty()

        StepVerifier.create(service.assignAvion(volId, avionId))
            .expectError(NotFoundException::class.java)
            .verify()
    }

    @Test
    fun `assignAvion succeeds`() {
        val volId = UUID.randomUUID()
        val avionId = UUID.randomUUID()
        val now = LocalDateTime.now()

        val current = Vol(
            volId, "AF4000", "CDG", "MAD",
            now.plusHours(1), now.plusHours(3),
            VolEtat.PREVU, null, null
        )

        val updated = current.copy(avionId = avionId)

        every { avionPort.findById(avionId) } returns Mono.just(mockk())
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

        val current = Vol(
            id, "AF5000", "MAD", "CDG",
            now.plusHours(1), now.plusHours(3),
            VolEtat.PREVU, UUID.randomUUID(), UUID.randomUUID()
        )

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
        val current = Vol(
            id, "AF6000", "CDG", "LIS",
            now.plusHours(1), now.plusHours(3),
            VolEtat.EN_ATTENTE, null, null
        )

        val target = VolEtat.EMBARQUEMENT
        val updated = current.copy(etat = target)

        every { volPort.findById(id) } returns Mono.just(current)
        every { strategy.canTransition(current.etat, target) } returns true
        every { volPort.save(any()) } returns Mono.just(updated)

        StepVerifier.create(service.updateEtat(id, target))
            .expectNextMatches { it.etat == target }
            .verifyComplete()
    }

    @Test
    fun `updateEtat fails when transition forbidden`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()

        val current = Vol(
            id, "AF6001", "LIS", "CDG",
            now.plusHours(1), now.plusHours(3),
            VolEtat.ARRIVE, null, null
        )

        val target = VolEtat.DECOLLE

        every { volPort.findById(id) } returns Mono.just(current)
        every { strategy.canTransition(current.etat, target) } returns false

        StepVerifier.create(service.updateEtat(id, target))
            .expectError(IllegalArgumentException::class.java)
            .verify()

        verify(exactly = 0) { volPort.save(any()) }
    }

    @Test
    fun `listByEtat delegates to port`() {
        val now = LocalDateTime.now()

        val v1 = Vol(
            UUID.randomUUID(), "AF7000", "CDG", "MAD",
            now.plusHours(2), now.plusHours(4), VolEtat.EN_VOL, null, null
        )

        every { volPort.findByEtat(VolEtat.EN_VOL) } returns Flux.just(v1)

        StepVerifier.create(service.listByEtat(VolEtat.EN_VOL))
            .expectNext(v1)
            .verifyComplete()

        verify { volPort.findByEtat(VolEtat.EN_VOL) }
    }
}
