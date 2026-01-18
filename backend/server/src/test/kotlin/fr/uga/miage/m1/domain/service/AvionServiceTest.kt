package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.HangarDataPort
import fr.uga.miage.m1.domain.port.VolDataPort
import fr.uga.miage.m1.exceptions.NotFoundException
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.UUID

class AvionServiceTest {

    private lateinit var avionPort: AvionDataPort
    private lateinit var hangarPort: HangarDataPort
    private lateinit var volPort: VolDataPort
    private lateinit var hangarService: HangarService
    private lateinit var service: AvionService

    @BeforeEach
    fun setup() {
        avionPort = mockk()
        hangarPort = mockk()
        volPort = mockk()
        hangarService = mockk()
        service = AvionService(avionPort, hangarPort, volPort, hangarService)
    }

    @Test
    fun `list returns all avions`() {
        val a = Avion(UUID.randomUUID(), "F-GRNB", "A320", 180, AvionEtat.EN_VOL, null)

        every { avionPort.findAll() } returns Flux.just(a)

        StepVerifier.create(service.list())
            .expectNext(a)
            .verifyComplete()
    }

    @Test
    fun `get returns avion`() {
        val id = UUID.randomUUID()
        val avion = Avion(id, "F-GRNB", "A320", 180, AvionEtat.EN_VOL, null)

        every { avionPort.findById(id) } returns Mono.just(avion)

        StepVerifier.create(service.get(id))
            .expectNext(avion)
            .verifyComplete()
    }

    @Test
    fun `get throws NotFoundException when missing`() {
        val id = UUID.randomUUID()

        every { avionPort.findById(id) } returns Mono.empty()

        StepVerifier.create(service.get(id))
            .expectError(NotFoundException::class.java)
            .verify()
    }

    @Test
    fun `create succeeds when hangar exists and has capacity`() {
        val hId = UUID.randomUUID()
        val hangar = Hangar(hId, "H1", 10, HangarEtat.DISPONIBLE)
        val avion = Avion(null, "F-GRNB", "A320", 180, AvionEtat.DISPONIBLE, hId)
        val saved = avion.copy(id = UUID.randomUUID())

        every { avionPort.existsByImmatriculation("F-GRNB") } returns Mono.just(false)
        every { hangarPort.existsById(hId) } returns Mono.just(true)
        every { hangarService.ensureCanAcceptAvion(hId) } returns Mono.just(hangar)
        every { avionPort.save(any()) } returns Mono.just(saved)
        every { hangarService.updateStateBasedOnOccupancy(hId) } returns Mono.just(hangar)

        StepVerifier.create(service.create(avion))
            .expectNext(saved)
            .verifyComplete()

        verify { hangarService.updateStateBasedOnOccupancy(hId) }
    }

    @Test
    fun `create throws when hangar missing`() {
        val hId = UUID.randomUUID()
        val avion = Avion(null, "F-GRNB", "A320", 180, AvionEtat.DISPONIBLE, hId)

        every { avionPort.existsByImmatriculation("F-GRNB") } returns Mono.just(false)
        every { hangarPort.existsById(hId) } returns Mono.just(false)

        StepVerifier.create(service.create(avion))
            .expectError(NotFoundException::class.java)
            .verify()
    }

    @Test
    fun `assignHangar works and updates both hangars`() {
        val id = UUID.randomUUID()
        val oldHangarId = UUID.randomUUID()
        val newHangarId = UUID.randomUUID()
        val hangar = Hangar(newHangarId, "H1", 10, HangarEtat.DISPONIBLE)
        val oldHangar = Hangar(oldHangarId, "H2", 10, HangarEtat.DISPONIBLE)
        val avion = Avion(id, "F-GRNB", "A320", 180, AvionEtat.MAINTENANCE, oldHangarId)
        val updated = avion.copy(hangarId = newHangarId)

        every { hangarService.ensureCanAcceptAvion(newHangarId) } returns Mono.just(hangar)
        every { avionPort.findById(id) } returns Mono.just(avion)
        every { avionPort.save(any()) } returns Mono.just(updated)
        every { hangarService.updateStateBasedOnOccupancy(newHangarId) } returns Mono.just(hangar)
        every { hangarService.updateStateBasedOnOccupancy(oldHangarId) } returns Mono.just(oldHangar)

        StepVerifier.create(service.assignHangar(id, newHangarId))
            .expectNextMatches { it.hangarId == newHangarId }
            .verifyComplete()

        verify { hangarService.updateStateBasedOnOccupancy(newHangarId) }
        verify { hangarService.updateStateBasedOnOccupancy(oldHangarId) }
    }

    @Test
    fun `create succeeds when hangarId is null`() {
        val avion = Avion(null, "F-GRNB", "A320", 180, AvionEtat.EN_VOL, null)
        val saved = avion.copy(id = UUID.randomUUID())

        every { avionPort.existsByImmatriculation("F-GRNB") } returns Mono.just(false)
        every { avionPort.save(any()) } returns Mono.just(saved)

        StepVerifier.create(service.create(avion))
            .expectNext(saved)
            .verifyComplete()

        verify(exactly = 0) { hangarService.updateStateBasedOnOccupancy(any()) }
    }

    @Test
    fun `update succeeds when avion exists and hangarId is null`() {
        val id = UUID.randomUUID()
        val current = Avion(id, "F-GRNB", "A320", 180, AvionEtat.EN_VOL, null)
        val new = Avion(null, "F-GRNB", "A330", 200, AvionEtat.EN_VOL, null)
        val updated = current.copy(type = "A330", capacite = 200)

        every { avionPort.findById(id) } returns Mono.just(current)
        every { volPort.existsByAvionIdAndEtatIn(id, any()) } returns Mono.just(false)
        every { avionPort.save(updated) } returns Mono.just(updated)

        StepVerifier.create(service.update(id, new))
            .expectNext(updated)
            .verifyComplete()

        verify(exactly = 0) { hangarPort.existsById(any()) }
    }

    @Test
    fun `delete delegates to port and updates hangar when no active flights`() {
        val id = UUID.randomUUID()
        val hangarId = UUID.randomUUID()
        val hangar = Hangar(hangarId, "H1", 10, HangarEtat.DISPONIBLE)
        val avion = Avion(id, "F-GRNB", "A320", 180, AvionEtat.DISPONIBLE, hangarId)

        every { avionPort.findById(id) } returns Mono.just(avion)
        every { volPort.existsByAvionIdAndEtatIn(id, any()) } returns Mono.just(false)
        every { avionPort.deleteById(id) } returns Mono.just(Unit)
        every { hangarService.updateStateBasedOnOccupancy(hangarId) } returns Mono.just(hangar)

        StepVerifier.create(service.delete(id))
            .expectNext(Unit)
            .verifyComplete()

        verify { avionPort.deleteById(id) }
        verify { hangarService.updateStateBasedOnOccupancy(hangarId) }
    }

    @Test
    fun `unassignHangar clears hangarId and updates old hangar`() {
        val id = UUID.randomUUID()
        val hangarId = UUID.randomUUID()
        val hangar = Hangar(hangarId, "H1", 10, HangarEtat.DISPONIBLE)
        val current = Avion(id, "F-GRNB", "A320", 180, AvionEtat.EN_VOL, hangarId)
        val updated = current.copy(hangarId = null)

        every { avionPort.findById(id) } returns Mono.just(current)
        every { avionPort.save(updated) } returns Mono.just(updated)
        every { hangarService.updateStateBasedOnOccupancy(hangarId) } returns Mono.just(hangar)

        StepVerifier.create(service.unassignHangar(id))
            .expectNextMatches { it.hangarId == null }
            .verifyComplete()

        verify { hangarService.updateStateBasedOnOccupancy(hangarId) }
    }
}
