package fr.uga.miage.m1.persistence.adapter

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.persistence.entity.AvionEntity
import fr.uga.miage.m1.persistence.repository.AvionRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class AvionAdapterTest {

    private lateinit var repo: AvionRepository
    private lateinit var adapter: AvionAdapter

    @BeforeEach
    fun setUp() {
        repo = mockk()
        adapter = AvionAdapter(repo)
    }

    @Test
    fun `findAll maps entity to domain`() {
        val e1 = AvionEntity(
            id = UUID.randomUUID(),
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_VOL,
            hangarId = null
        )

        every { repo.findAll() } returns Flux.just(e1)

        StepVerifier.create(adapter.findAll())
            .expectNextMatches { it.immatriculation == "F-GRNB" }
            .verifyComplete()

        verify { repo.findAll() }
    }

    @Test
    fun `findById returns mapped domain`() {
        val id = UUID.randomUUID()
        val entity = AvionEntity(id, "F-GRNB", "A320", 180, AvionEtat.EN_VOL, null)

        every { repo.findById(id) } returns Mono.just(entity)

        StepVerifier.create(adapter.findById(id))
            .expectNextMatches { it.id == id && it.type == "A320" }
            .verifyComplete()

        verify { repo.findById(id) }
    }

    @Test
    fun `findById empty returns empty`() {
        val id = UUID.randomUUID()
        every { repo.findById(id) } returns Mono.empty()

        StepVerifier.create(adapter.findById(id))
            .verifyComplete()
    }

    @Test
    fun `save maps domain to entity and back`() {
        val id = UUID.randomUUID()
        val domain = Avion(id, "F-GRNB", "A320", 180, AvionEtat.EN_VOL, null)
        val entity = AvionEntity(id, "F-GRNB", "A320", 180, AvionEtat.EN_VOL, null)

        every { repo.save(entity) } returns Mono.just(entity)

        StepVerifier.create(adapter.save(domain))
            .expectNextMatches { it.id == id && it.type == "A320" }
            .verifyComplete()

        verify { repo.save(match { it.immatriculation == "F-GRNB" }) }
    }

    @Test
    fun `existsByImmatriculation delegates to repo`() {
        every { repo.existsByImmatriculation("F-GRNB") } returns Mono.just(true)

        StepVerifier.create(adapter.existsByImmatriculation("F-GRNB"))
            .expectNext(true)
            .verifyComplete()

        verify { repo.existsByImmatriculation("F-GRNB") }
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
        val e1 = AvionEntity(
            id = UUID.randomUUID(),
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_VOL,
            hangarId = null
        )

        every { repo.findByEtat(AvionEtat.EN_VOL) } returns Flux.just(e1)

        StepVerifier.create(adapter.findByEtat(AvionEtat.EN_VOL))
            .expectNextMatches { it.immatriculation == "F-GRNB" && it.type == "A320" }
            .verifyComplete()

        verify { repo.findByEtat(AvionEtat.EN_VOL) }
    }

    @Test
    fun `findByImmatriculation returns mapped domain`() {
        val entity = AvionEntity(
            id = UUID.randomUUID(),
            immatriculation = "F-TEST",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_VOL,
            hangarId = null
        )

        every { repo.findByImmatriculation("F-TEST") } returns Mono.just(entity)

        StepVerifier.create(adapter.findByImmatriculation("F-TEST"))
            .expectNextMatches { it.immatriculation == "F-TEST" && it.type == "A320" }
            .verifyComplete()

        verify { repo.findByImmatriculation("F-TEST") }
    }


}
