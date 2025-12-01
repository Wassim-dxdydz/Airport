package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.HangarDataPort
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
    private lateinit var service: AvionService

    @BeforeEach
    fun setup() {
        avionPort = mockk()
        hangarPort = mockk()
        service = AvionService(avionPort, hangarPort)
    }

    @Test
    fun `list returns all avions`() {
        val a = Avion(UUID.randomUUID(), "F-GRNB", "A320", 180, AvionEtat.EN_SERVICE, null)

        every { avionPort.findAll() } returns Flux.just(a)

        StepVerifier.create(service.list())
            .expectNext(a)
            .verifyComplete()
    }

    @Test
    fun `get returns avion`() {
        val id = UUID.randomUUID()
        val avion = Avion(id, "F-GRNB", "A320", 180, AvionEtat.EN_SERVICE, null)

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
    fun `create succeeds when hangar exists`() {
        val hId = UUID.randomUUID()
        val avion = Avion(null, "F-GRNB", "A320", 180, AvionEtat.EN_SERVICE, hId)
        val saved = avion.copy(id = UUID.randomUUID())

        every { hangarPort.existsById(hId) } returns Mono.just(true)
        every { avionPort.save(avion) } returns Mono.just(saved)

        StepVerifier.create(service.create(avion))
            .expectNext(saved)
            .verifyComplete()
    }

    @Test
    fun `create throws when hangar missing`() {
        val hId = UUID.randomUUID()
        val avion = Avion(null, "F-GRNB", "A320", 180, AvionEtat.EN_SERVICE, hId)

        every { hangarPort.existsById(hId) } returns Mono.just(false)

        StepVerifier.create(service.create(avion))
            .expectError(NotFoundException::class.java)
            .verify()
    }

    @Test
    fun `assignHangar works`() {
        val id = UUID.randomUUID()
        val hId = UUID.randomUUID()
        val avion = Avion(id, "F-GRNB", "A320", 180, AvionEtat.EN_SERVICE, null)

        every { hangarPort.existsById(hId) } returns Mono.just(true)
        every { avionPort.findById(id) } returns Mono.just(avion)
        every { avionPort.save(any()) } returns Mono.just(avion.copy(hangarId = hId))

        StepVerifier.create(service.assignHangar(id, hId))
            .expectNextMatches { it.hangarId == hId }
            .verifyComplete()
    }
}
