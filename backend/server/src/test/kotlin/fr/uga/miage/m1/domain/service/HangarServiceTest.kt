package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.domain.port.HangarDataPort
import fr.uga.miage.m1.domain.port.AvionDataPort
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
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
    fun `get throws NotFoundException when missing`() {
        val id = UUID.randomUUID()
        every { hangarPort.findById(id) } returns Mono.empty()

        StepVerifier.create(service.get(id))
            .expectErrorSatisfies { assert(it is fr.uga.miage.m1.exceptions.NotFoundException) }
            .verify()
    }

    @Test
    fun `create saves and returns hangar`() {
        val h = Hangar(null, "H1", 10, HangarEtat.DISPONIBLE)
        val saved = h.copy(id = UUID.randomUUID())

        every { hangarPort.save(h) } returns Mono.just(saved)

        StepVerifier.create(service.create(h))
            .expectNext(saved)
            .verifyComplete()

        verify { hangarPort.save(h) }
    }

    @Test
    fun `update hangar`() {
        val id = UUID.randomUUID()
        val current = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)
        val updated = current.copy(capacite = 20)

        every { hangarPort.findById(id) } returns Mono.just(current)
        every { hangarPort.save(any()) } returns Mono.just(updated)

        StepVerifier.create(service.update(id, updated))
            .expectNext(updated)
            .verifyComplete()
    }

    @Test
    fun `delete delegates to port`() {
        val id = UUID.randomUUID()
        val hangar = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)

        every { hangarPort.findById(id) } returns Mono.just(hangar)
        every { avionPort.findAll() } returns Flux.empty()
        every { hangarPort.deleteById(id) } returns Mono.just(Unit)

        StepVerifier.create(service.delete(id))
            .expectNext(Unit)
            .verifyComplete()

        verify {
            hangarPort.findById(id)
            avionPort.findAll()
            hangarPort.deleteById(id)
        }
    }




    @Test
    fun `listAvions returns only avions in hangar`() {
        val id = UUID.randomUUID()
        val a1 = fr.uga.miage.m1.domain.model.Avion(UUID.randomUUID(), "A", "T", 10, AvionEtat.EN_SERVICE, id)
        val a2 = fr.uga.miage.m1.domain.model.Avion(UUID.randomUUID(), "B", "U", 20, AvionEtat.EN_SERVICE, null)

        every { avionPort.findAll() } returns Flux.just(a1, a2)

        StepVerifier.create(service.listAvions(id))
            .expectNext(a1)
            .verifyComplete()

        verify { avionPort.findAll() }
    }

}
