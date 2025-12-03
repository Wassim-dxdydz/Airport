package fr.uga.miage.m1.persistence.adapter

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.domain.model.Piste
import fr.uga.miage.m1.persistence.entity.PisteEntity
import fr.uga.miage.m1.persistence.repository.PisteRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class PisteAdapterTest {

    private lateinit var repo: PisteRepository
    private lateinit var adapter: PisteAdapter

    @BeforeEach
    fun setUp() {
        repo = mockk()
        adapter = PisteAdapter(repo)
    }

    @Test
    fun `findAll maps entities`() {
        val e = PisteEntity(UUID.randomUUID(), "R1", 3200, PisteEtat.LIBRE)
        every { repo.findAll() } returns Flux.just(e)

        StepVerifier.create(adapter.findAll())
            .expectNextMatches { it.identifiant == "R1" }
            .verifyComplete()
    }

    @Test
    fun `save delegates`() {
        val id = UUID.randomUUID()
        val domain = Piste(id, "R1", 3000, PisteEtat.LIBRE)
        val entity = PisteEntity(id, "R1", 3000, PisteEtat.LIBRE)

        every { repo.save(entity) } returns Mono.just(entity)

        StepVerifier.create(adapter.save(domain))
            .expectNextMatches { it.longueurM == 3000 }
            .verifyComplete()
    }

    @Test
    fun `findById maps entity to domain`() {
        val id = UUID.randomUUID()
        val entity = PisteEntity(id, "R1", 3000, PisteEtat.LIBRE)

        every { repo.findById(id) } returns Mono.just(entity)

        StepVerifier.create(adapter.findById(id))
            .expectNextMatches { it.id == id && it.identifiant == "R1" }
            .verifyComplete()

        verify { repo.findById(id) }
    }

    @Test
    fun `deleteById delegates to repo`() {
        val id = UUID.randomUUID()
        every { repo.deleteById(id) } returns Mono.empty()

        StepVerifier.create(adapter.deleteById(id))
            .expectNext(Unit)
            .verifyComplete()

        verify { repo.deleteById(id) }
    }

    @Test
    fun `findByEtat maps entities to domain`() {
        val entity = PisteEntity(UUID.randomUUID(), "R1", 3200, PisteEtat.LIBRE)
        every { repo.findByEtat(PisteEtat.LIBRE) } returns Flux.just(entity)

        StepVerifier.create(adapter.findByEtat(PisteEtat.LIBRE))
            .expectNextMatches { it.identifiant == "R1" && it.etat == PisteEtat.LIBRE }
            .verifyComplete()

        verify { repo.findByEtat(PisteEtat.LIBRE) }
    }

    @Test
    fun `existsById delegates to repo`() {
        val id = UUID.randomUUID()
        every { repo.existsById(id) } returns Mono.just(true)

        StepVerifier.create(adapter.existsById(id))
            .expectNext(true)
            .verifyComplete()

        verify { repo.existsById(id) }
    }

}
