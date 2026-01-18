package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.HangarDataPort
import fr.uga.miage.m1.exceptions.NotFoundException
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class HangarServiceTest {

    private lateinit var hangarPort: HangarDataPort
    private lateinit var avionPort: AvionDataPort
    private lateinit var service: HangarService

    @BeforeEach
    fun setup() {
        hangarPort = mockk()
        avionPort = mockk()
        service = HangarService(hangarPort, avionPort)
    }

    @Test
    fun `list returns all hangars`() {
        val h = Hangar(UUID.randomUUID(), "H1", 10, HangarEtat.DISPONIBLE)

        every { hangarPort.findAll() } returns Flux.just(h)

        StepVerifier.create(service.list())
            .expectNext(h)
            .verifyComplete()
    }

    @Test
    fun `get returns hangar when exists`() {
        val id = UUID.randomUUID()
        val hangar = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)

        every { hangarPort.findById(id) } returns Mono.just(hangar)

        StepVerifier.create(service.get(id))
            .expectNext(hangar)
            .verifyComplete()
    }

    @Test
    fun `get throws NotFoundException when missing`() {
        val id = UUID.randomUUID()
        every { hangarPort.findById(id) } returns Mono.empty()

        StepVerifier.create(service.get(id))
            .expectError(NotFoundException::class.java)
            .verify()
    }

    @Test
    fun `create succeeds with DISPONIBLE state`() {
        val h = Hangar(null, "H1", 10, HangarEtat.DISPONIBLE)
        val saved = h.copy(id = UUID.randomUUID())

        every { hangarPort.save(h) } returns Mono.just(saved)

        StepVerifier.create(service.create(h))
            .expectNext(saved)
            .verifyComplete()

        verify { hangarPort.save(h) }
    }

    @Test
    fun `create succeeds with MAINTENANCE state`() {
        val h = Hangar(null, "H2", 5, HangarEtat.MAINTENANCE)
        val saved = h.copy(id = UUID.randomUUID())

        every { hangarPort.save(h) } returns Mono.just(saved)

        StepVerifier.create(service.create(h))
            .expectNext(saved)
            .verifyComplete()
    }

    @Test
    fun `create throws when created with PLEIN state`() {
        val h = Hangar(null, "H1", 10, HangarEtat.PLEIN)

        StepVerifier.create(service.create(h))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("ne peut être créé qu'avec l'état DISPONIBLE ou MAINTENANCE")
            }
            .verify()
    }

    @Test
    fun `update succeeds when only identifiant changes`() {
        val id = UUID.randomUUID()
        val current = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)
        val updated = current.copy(identifiant = "H1-NEW")

        every { hangarPort.findById(id) } returns Mono.just(current)
        every { hangarPort.save(any()) } returns Mono.just(updated)

        StepVerifier.create(service.update(id, updated))
            .expectNextMatches { it.identifiant == "H1-NEW" }
            .verifyComplete()
    }

    @Test
    fun `update succeeds when increasing capacity`() {
        val id = UUID.randomUUID()
        val current = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)
        val updated = current.copy(capacite = 20)
        val saved = updated.copy(id = id)

        every { hangarPort.findById(id) } returns Mono.just(current)
        every { hangarPort.save(any()) } returns Mono.just(saved)
        every { avionPort.countByHangarId(id) } returns Mono.just(5L)

        StepVerifier.create(service.update(id, updated))
            .expectNextMatches { it.capacite == 20 }
            .verifyComplete()
    }

    @Test
    fun `update throws when reducing capacity below current avion count`() {
        val id = UUID.randomUUID()
        val current = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)
        val updated = current.copy(capacite = 3)

        every { hangarPort.findById(id) } returns Mono.just(current)
        every { avionPort.countByHangarId(id) } returns Mono.just(5L)

        StepVerifier.create(service.update(id, updated))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("Impossible de réduire la capacité")
            }
            .verify()
    }

    @Test
    fun `update throws when transitioning to MAINTENANCE with avions inside`() {
        val id = UUID.randomUUID()
        val current = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)
        val updated = current.copy(etat = HangarEtat.MAINTENANCE)

        every { hangarPort.findById(id) } returns Mono.just(current)
        every { avionPort.countByHangarId(id) } returns Mono.just(2L)

        StepVerifier.create(service.update(id, updated))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("Impossible de mettre le hangar en maintenance")
            }
            .verify()
    }

    @Test
    fun `update succeeds when transitioning to MAINTENANCE with empty hangar`() {
        val id = UUID.randomUUID()
        val current = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)
        val updated = current.copy(etat = HangarEtat.MAINTENANCE)
        val saved = updated.copy(id = id)

        every { hangarPort.findById(id) } returns Mono.just(current)
        every { avionPort.countByHangarId(id) } returns Mono.just(0L)
        every { hangarPort.save(any()) } returns Mono.just(saved)

        StepVerifier.create(service.update(id, updated))
            .expectNextMatches { it.etat == HangarEtat.MAINTENANCE }
            .verifyComplete()
    }

    @Test
    fun `update throws when manually setting DISPONIBLE state`() {
        val id = UUID.randomUUID()
        val current = Hangar(id, "H1", 10, HangarEtat.PLEIN)
        val updated = current.copy(etat = HangarEtat.DISPONIBLE)

        every { hangarPort.findById(id) } returns Mono.just(current)

        StepVerifier.create(service.update(id, updated))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("Impossible de modifier manuellement l'état")
            }
            .verify()
    }

    @Test
    fun `delete succeeds when hangar is empty`() {
        val id = UUID.randomUUID()
        val hangar = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)

        every { hangarPort.findById(id) } returns Mono.just(hangar)
        every { avionPort.findAll() } returns Flux.empty()
        every { hangarPort.deleteById(id) } returns Mono.just(Unit)

        StepVerifier.create(service.delete(id))
            .expectNext(Unit)
            .verifyComplete()

        verify { hangarPort.deleteById(id) }
    }

    @Test
    fun `delete throws when hangar contains avions`() {
        val id = UUID.randomUUID()
        val hangar = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)
        val avion = Avion(UUID.randomUUID(), "F-GRNB", "A320", 180, AvionEtat.DISPONIBLE, id)

        every { hangarPort.findById(id) } returns Mono.just(hangar)
        every { avionPort.findAll() } returns Flux.just(avion)

        StepVerifier.create(service.delete(id))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("il contient encore des avions")
            }
            .verify()

        verify(exactly = 0) { hangarPort.deleteById(id) }
    }

    @Test
    fun `listAvions returns only avions in hangar`() {
        val id = UUID.randomUUID()
        val a1 = Avion(UUID.randomUUID(), "F-GRNB", "A320", 180, AvionEtat.DISPONIBLE, id)
        val a2 = Avion(UUID.randomUUID(), "F-TEST", "A330", 250, AvionEtat.EN_VOL, null)

        every { avionPort.findAll() } returns Flux.just(a1, a2)

        StepVerifier.create(service.listAvions(id))
            .expectNext(a1)
            .verifyComplete()

        verify { avionPort.findAll() }
    }

    @Test
    fun `ensureCanAcceptAvion succeeds when hangar is DISPONIBLE with space`() {
        val id = UUID.randomUUID()
        val hangar = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)

        every { hangarPort.findById(id) } returns Mono.just(hangar)
        every { avionPort.countByHangarId(id) } returns Mono.just(5L)

        StepVerifier.create(service.ensureCanAcceptAvion(id))
            .expectNext(hangar)
            .verifyComplete()
    }

    @Test
    fun `ensureCanAcceptAvion throws when hangar is in MAINTENANCE`() {
        val id = UUID.randomUUID()
        val hangar = Hangar(id, "H1", 10, HangarEtat.MAINTENANCE)

        every { hangarPort.findById(id) } returns Mono.just(hangar)

        StepVerifier.create(service.ensureCanAcceptAvion(id))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("le hangar est en maintenance")
            }
            .verify()
    }

    @Test
    fun `ensureCanAcceptAvion throws when hangar is PLEIN`() {
        val id = UUID.randomUUID()
        val hangar = Hangar(id, "H1", 10, HangarEtat.PLEIN)

        every { hangarPort.findById(id) } returns Mono.just(hangar)

        StepVerifier.create(service.ensureCanAcceptAvion(id))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("le hangar est plein")
            }
            .verify()
    }

}
