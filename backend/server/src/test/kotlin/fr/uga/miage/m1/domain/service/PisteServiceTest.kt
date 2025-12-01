package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.domain.model.Piste
import fr.uga.miage.m1.domain.port.PisteDataPort
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class PisteServiceTest {

    private lateinit var pistePort: PisteDataPort
    private lateinit var service: PisteService

    @BeforeEach
    fun setup() {
        pistePort = mockk()
        service = PisteService(pistePort)
    }

    @Test
    fun `list returns pistes`() {
        val p = Piste(UUID.randomUUID(), "R1", 3000, PisteEtat.LIBRE)

        every { pistePort.findAll() } returns Flux.just(p)

        StepVerifier.create(service.list())
            .expectNext(p)
            .verifyComplete()
    }

    @Test
    fun `updateEtat updates piste status`() {
        val id = UUID.randomUUID()
        val current = Piste(id, "R1", 3000, PisteEtat.LIBRE)
        val updated = current.copy(etat = PisteEtat.OCCUPEE)

        every { pistePort.findById(id) } returns Mono.just(current)
        every { pistePort.save(any()) } returns Mono.just(updated)

        StepVerifier.create(service.updateEtat(id, PisteEtat.OCCUPEE))
            .expectNext(updated)
            .verifyComplete()
    }
}
