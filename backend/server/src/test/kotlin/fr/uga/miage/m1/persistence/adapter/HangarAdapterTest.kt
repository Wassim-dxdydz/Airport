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
    fun `findAll maps entities to domain`() {
        val entity = HangarEntity(
            id = UUID.randomUUID(),
            identifiant = "H1",
            capacite = 10,
            etat = HangarEtat.DISPONIBLE
        )

        every { repo.findAll() } returns Flux.just(entity)

        StepVerifier.create(adapter.findAll())
            .expectNextMatches { it.identifiant == "H1" && it.capacite == 10 }
            .verifyComplete()

        verify { repo.findAll() }
    }

    @Test
    fun `findById returns mapped domain`() {
        val id = UUID.randomUUID()
        val entity = HangarEntity(
            id = id,
            identifiant = "H2",
            capacite = 15,
            etat = HangarEtat.DISPONIBLE
        )

        every { repo.findById(id) } returns Mono.just(entity)

        StepVerifier.create(adapter.findById(id))
            .expectNextMatches { it.id == id && it.identifiant == "H2" }
            .verifyComplete()

        verify { repo.findById(id) }
    }

    @Test
    fun `findById returns empty when not found`() {
        val id = UUID.randomUUID()
        every { repo.findById(id) } returns Mono.empty()

        StepVerifier.create(adapter.findById(id))
            .verifyComplete()
    }

    @Test
    fun `findByIdentifiant returns mapped domain`() {
        val entity = HangarEntity(
            id = UUID.randomUUID(),
            identifiant = "H3",
            capacite = 8,
            etat = HangarEtat.MAINTENANCE
        )

        every { repo.findByIdentifiant("H3") } returns Mono.just(entity)

        StepVerifier.create(adapter.findByIdentifiant("H3"))
            .expectNextMatches { it.identifiant == "H3" && it.etat == HangarEtat.MAINTENANCE }
            .verifyComplete()

        verify { repo.findByIdentifiant("H3") }
    }

    @Test
    fun `save maps domain to entity and back`() {
        val id = UUID.randomUUID()
        val domain = Hangar(
            id = id,
            identifiant = "H4",
            capacite = 12,
            etat = HangarEtat.DISPONIBLE
        )
        val entity = HangarEntity(
            id = id,
            identifiant = "H4",
            capacite = 12,
            etat = HangarEtat.DISPONIBLE
        )

        every { repo.save(any()) } returns Mono.just(entity)

        StepVerifier.create(adapter.save(domain))
            .expectNextMatches { it.id == id && it.identifiant == "H4" }
            .verifyComplete()

        verify { repo.save(match { it.identifiant == "H4" && it.capacite == 12 }) }
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
    fun `deleteByIdentifiant delegates to repo`() {
        every { repo.deleteByIdentifiant("H5") } returns Mono.just(Unit)

        StepVerifier.create(adapter.deleteByIdentifiant("H5"))
            .expectNext(Unit)
            .verifyComplete()

        verify { repo.deleteByIdentifiant("H5") }
    }

    @Test
    fun `findAllByIds returns mapped domains`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val ids = setOf(id1, id2)

        val entity1 = HangarEntity(id1, "H10", 10, HangarEtat.DISPONIBLE)
        val entity2 = HangarEntity(id2, "H11", 15, HangarEtat.DISPONIBLE)

        every { repo.findAllById(ids) } returns Flux.just(entity1, entity2)

        StepVerifier.create(adapter.findAllByIds(ids).collectList())
            .expectNextMatches {
                it.size == 2 &&
                        it.any { h -> h.identifiant == "H10" } &&
                        it.any { h -> h.identifiant == "H11" }
            }
            .verifyComplete()

        verify { repo.findAllById(ids) }
    }
}
