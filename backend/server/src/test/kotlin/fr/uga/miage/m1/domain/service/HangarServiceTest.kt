package fr.uga.miage.m1.domain.service

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
    fun `update updates hangar`() {
        val id = UUID.randomUUID()
        val current = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)
        val updated = current.copy(capacite = 20)

        every { hangarPort.findById(id) } returns Mono.just(current)
        every { hangarPort.save(any()) } returns Mono.just(updated)

        StepVerifier.create(service.update(id, updated))
            .expectNext(updated)
            .verifyComplete()
    }
}
