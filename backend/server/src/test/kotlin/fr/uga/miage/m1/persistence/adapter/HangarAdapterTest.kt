package fr.uga.miage.m1.persistence.adapter

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.persistence.entity.HangarEntity
import fr.uga.miage.m1.persistence.repository.HangarRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class HangarAdapterTest {

    private lateinit var repo: HangarRepository
    private lateinit var adapter: HangarAdapter

    @BeforeEach
    fun setUp() {
        repo = mockk()
        adapter = HangarAdapter(repo)
    }

    @Test
    fun `findAll maps entity to domain`() {
        val entity = HangarEntity(UUID.randomUUID(), "H1", 10, HangarEtat.DISPONIBLE)
        every { repo.findAll() } returns Flux.just(entity)

        StepVerifier.create(adapter.findAll())
            .expectNextMatches { it.identifiant == "H1" }
            .verifyComplete()

        verify { repo.findAll() }
    }

    @Test
    fun `findById mapped`() {
        val id = UUID.randomUUID()
        val entity = HangarEntity(id, "H1", 10, HangarEtat.DISPONIBLE)
        every { repo.findById(id) } returns Mono.just(entity)

        StepVerifier.create(adapter.findById(id))
            .expectNextMatches { it.id == id }
            .verifyComplete()
    }

    @Test
    fun `save delegates to repo`() {
        val id = UUID.randomUUID()
        val domain = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)
        val entity = HangarEntity(id, "H1", 10, HangarEtat.DISPONIBLE)

        every { repo.save(entity) } returns Mono.just(entity)

        StepVerifier.create(adapter.save(domain))
            .expectNextMatches { it.identifiant == "H1" }
            .verifyComplete()

        verify { repo.save(any()) }
    }

    @Test
    fun `existsById delegates`() {
        val id = UUID.randomUUID()
        every { repo.existsById(id) } returns Mono.just(true)

        StepVerifier.create(adapter.existsById(id))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `findByIdentifiant maps correctly`() {
        val entity = HangarEntity(UUID.randomUUID(), "H1", 20, HangarEtat.DISPONIBLE)

        every { repo.findByIdentifiant("H1") } returns Mono.just(entity)

        StepVerifier.create(adapter.findByIdentifiant("H1"))
            .expectNextMatches { it.identifiant == "H1" && it.capacite == 20 }
            .verifyComplete()

        verify { repo.findByIdentifiant("H1") }
    }

    @Test
    fun `deleteByIdentifiant delegates to repo`() {
        every { repo.deleteByIdentifiant("H1") } returns Mono.empty()

        StepVerifier.create(adapter.deleteByIdentifiant("H1"))
            .expectNext(Unit)
            .verifyComplete()

        verify { repo.deleteByIdentifiant("H1") }
    }

    @Test
    fun `deleteById delegates`() {
        val id = UUID.randomUUID()
        every { repo.deleteById(id) } returns Mono.empty()

        StepVerifier.create(adapter.deleteById(id))
            .expectNext(Unit)
            .verifyComplete()

        verify { repo.deleteById(id) }
    }

}
