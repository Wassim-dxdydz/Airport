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
    fun `create succeeds with LIBRE state`() {
        val p = Piste(null, "R2", 2500, PisteEtat.LIBRE)
        val saved = p.copy(id = UUID.randomUUID())
        every { pistePort.save(p) } returns Mono.just(saved)

        StepVerifier.create(service.create(p))
            .expectNext(saved)
            .verifyComplete()
    }

    @Test
    fun `create throws when state is not LIBRE or MAINTENANCE`() {
        val p = Piste(null, "R2", 2500, PisteEtat.OCCUPEE)

        StepVerifier.create(service.create(p))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("ne peut être créée qu'avec l'état LIBRE ou MAINTENANCE")
            }
            .verify()
    }

    @Test
    fun `delete succeeds when piste is not OCCUPEE`() {
        val id = UUID.randomUUID()
        val piste = Piste(id, "R1", 3000, PisteEtat.LIBRE)

        every { pistePort.findById(id) } returns Mono.just(piste)
        every { pistePort.deleteById(id) } returns Mono.empty()

        StepVerifier.create(service.delete(id))
            .expectNext(Unit)
            .verifyComplete()
    }

    @Test
    fun `delete throws when piste is OCCUPEE`() {
        val id = UUID.randomUUID()
        val piste = Piste(id, "R1", 3000, PisteEtat.OCCUPEE)

        every { pistePort.findById(id) } returns Mono.just(piste)

        StepVerifier.create(service.delete(id))
            .expectErrorMatches {
                it is IllegalStateException &&
                        it.message!!.contains("Impossible de supprimer une piste encore occupée")
            }
            .verify()
    }

    @Test
    fun `disponibles returns only LIBRE pistes`() {
        val p = Piste(UUID.randomUUID(), "R1", 3000, PisteEtat.LIBRE)
        every { pistePort.findByEtat(PisteEtat.LIBRE) } returns Flux.just(p)

        StepVerifier.create(service.disponibles())
            .expectNext(p)
            .verifyComplete()
    }
}
