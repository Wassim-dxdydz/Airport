package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.domain.model.Piste
import fr.uga.miage.m1.domain.port.PisteDataPort
import fr.uga.miage.m1.exceptions.NotFoundException
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
    fun `get returns piste when found`() {
        val id = UUID.randomUUID()
        val p = Piste(id, "R1", 3000, PisteEtat.LIBRE)
        every { pistePort.findById(id) } returns Mono.just(p)

        StepVerifier.create(service.get(id))
            .expectNext(p)
            .verifyComplete()

        verify { pistePort.findById(id) }
    }

    @Test
    fun `get throws NotFoundException when not found`() {
        val id = UUID.randomUUID()
        every { pistePort.findById(id) } returns Mono.empty()

        StepVerifier.create(service.get(id))
            .expectError(NotFoundException::class.java)
            .verify()
    }

    @Test
    fun `create saves piste`() {
        val p = Piste(null, "R2", 2500, PisteEtat.LIBRE)
        val saved = p.copy(id = UUID.randomUUID())
        every { pistePort.save(p) } returns Mono.just(saved)

        StepVerifier.create(service.create(p))
            .expectNext(saved)
            .verifyComplete()

        verify { pistePort.save(p) }
    }

    @Test
    fun `updateEtat updates piste status`() {
        val id = UUID.randomUUID()
        val current = Piste(id, "R1", 3000, PisteEtat.LIBRE)
        val updated = current.copy(etat = PisteEtat.OCCUPEE)

        every { pistePort.findById(id) } returns Mono.just(current)
        every { pistePort.save(updated) } returns Mono.just(updated)

        StepVerifier.create(service.updateEtat(id, PisteEtat.OCCUPEE))
            .expectNext(updated)
            .verifyComplete()
    }

    @Test
    fun `delete deletes piste when libre`() {
        val id = UUID.randomUUID()
        val piste = Piste(id, "R1", 3000, PisteEtat.LIBRE)

        every { pistePort.findById(id) } returns Mono.just(piste)
        every { pistePort.deleteById(id) } returns Mono.empty()

        StepVerifier.create(service.delete(id))
            .expectNext(Unit)
            .verifyComplete()

        verify { pistePort.deleteById(id) }
    }


    @Test
    fun `delete throws when piste is occupee`() {
        val id = UUID.randomUUID()
        val piste = Piste(id, "R1", 3000, PisteEtat.OCCUPEE)

        every { pistePort.findById(id) } returns Mono.just(piste)

        StepVerifier.create(service.delete(id))
            .expectError(IllegalStateException::class.java)
            .verify()
    }

    @Test
    fun `disponibles returns free pistes`() {
        val p = Piste(UUID.randomUUID(), "R1", 3000, PisteEtat.LIBRE)
        every { pistePort.findByEtat(PisteEtat.LIBRE) } returns Flux.just(p)

        StepVerifier.create(service.disponibles())
            .expectNext(p)
            .verifyComplete()

        verify { pistePort.findByEtat(PisteEtat.LIBRE) }
    }
}
