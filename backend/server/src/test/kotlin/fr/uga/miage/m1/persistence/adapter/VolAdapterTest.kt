package fr.uga.miage.m1.persistence.adapter

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.persistence.entity.VolEntity
import fr.uga.miage.m1.persistence.repository.VolRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.UUID

class VolAdapterTest {

    private lateinit var repo: VolRepository
    private lateinit var adapter: VolAdapter

    @BeforeEach
    fun setUp() {
        repo = mockk()
        adapter = VolAdapter(repo)
    }

    @Test
    fun `findAll maps`() {
        val now = LocalDateTime.now()
        val e = VolEntity(
            id = UUID.randomUUID(),
            numeroVol = "AF1000",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            createdAt = now,
            updatedAt = now.plusMinutes(5)
        )

        every { repo.findAll() } returns Flux.just(e)

        StepVerifier.create(adapter.findAll())
            .expectNextMatches { it.numeroVol == "AF1000" }
            .verifyComplete()
    }

    @Test
    fun `save maps back`() {
        val now = LocalDateTime.now()
        val domain = Vol(
            id = null,
            numeroVol = "AF1000",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        val entity = VolEntity(
            id = null,
            numeroVol = "AF1000",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            createdAt = null,
            updatedAt = null
        )

        every { repo.save(any()) } returns Mono.just(entity)

        StepVerifier.create(adapter.save(domain))
            .expectNextMatches { it.numeroVol == "AF1000" }
            .verifyComplete()

        verify {
            repo.save(match {
                it.numeroVol == "AF1000" &&
                        it.createdAt == null &&
                        it.updatedAt == null
            })
        }
    }

    @Test
    fun `findById maps to domain`() {
        val now = LocalDateTime.now()
        val id = UUID.randomUUID()
        val entity = VolEntity(
            id, "AF1000", "CDG", "MAD",
            now, now.plusHours(2),
            VolEtat.PREVU, null, null, now, now
        )

        every { repo.findById(id) } returns Mono.just(entity)

        StepVerifier.create(adapter.findById(id))
            .expectNextMatches { it.id == id && it.numeroVol == "AF1000" }
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
    fun `findByNumeroVol maps`() {
        val now = LocalDateTime.now()
        val entity = VolEntity(
            UUID.randomUUID(), "AF1000", "CDG", "MAD",
            now, now.plusHours(2),
            VolEtat.PREVU, null, null, now, now
        )

        every { repo.findByNumeroVol("AF1000") } returns Mono.just(entity)

        StepVerifier.create(adapter.findByNumeroVol("AF1000"))
            .expectNextMatches { it.numeroVol == "AF1000" }
            .verifyComplete()

        verify { repo.findByNumeroVol("AF1000") }
    }

    @Test
    fun `deleteByNumeroVol delegates`() {
        every { repo.deleteByNumeroVol("AF1000") } returns Mono.empty()

        StepVerifier.create(adapter.deleteByNumeroVol("AF1000"))
            .expectNext(Unit)
            .verifyComplete()

        verify { repo.deleteByNumeroVol("AF1000") }
    }

    @Test
    fun `findByEtat maps entities`() {
        val now = LocalDateTime.now()
        val entity = VolEntity(
            UUID.randomUUID(), "AF1000", "CDG", "MAD",
            now, now.plusHours(2),
            VolEtat.PREVU, null, null ,now, now
        )

        every { repo.findByEtat(VolEtat.PREVU) } returns Flux.just(entity)

        StepVerifier.create(adapter.findByEtat(VolEtat.PREVU))
            .expectNextMatches { it.etat == VolEtat.PREVU }
            .verifyComplete()

        verify { repo.findByEtat(VolEtat.PREVU) }
    }

    @Test
    fun `findByAvionId maps entities`() {
        val now = LocalDateTime.now()
        val avionId = UUID.randomUUID()
        val entity = VolEntity(
            UUID.randomUUID(), "AF2000", "LYS", "NCE",
            now, now.plusHours(1),
            VolEtat.PREVU, avionId, null, now, now
        )

        every { repo.findByAvionId(avionId) } returns Flux.just(entity)

        StepVerifier.create(adapter.findByAvionId(avionId))
            .expectNextMatches { it.avionId == avionId }
            .verifyComplete()

        verify { repo.findByAvionId(avionId) }
    }

    @Test
    fun `findByOrigine maps entities`() {
        val now = LocalDateTime.now()
        val entity = VolEntity(
            UUID.randomUUID(), "AF3000", "LYS", "MAD",
            now, now.plusHours(2),
            VolEtat.PREVU, null, null, now, now
        )

        every { repo.findByOrigine("LYS") } returns Flux.just(entity)

        StepVerifier.create(adapter.findByOrigine("LYS"))
            .expectNextMatches { it.origine == "LYS" }
            .verifyComplete()

        verify { repo.findByOrigine("LYS") }
    }

    @Test
    fun `findByDestination maps entities`() {
        val now = LocalDateTime.now()
        val entity = VolEntity(
            UUID.randomUUID(), "AF4000", "CDG", "NYC",
            now, now.plusHours(8),
            VolEtat.PREVU, null, null, now, now
        )

        every { repo.findByDestination("NYC") } returns Flux.just(entity)

        StepVerifier.create(adapter.findByDestination("NYC"))
            .expectNextMatches { it.destination == "NYC" }
            .verifyComplete()

        verify { repo.findByDestination("NYC") }
    }

    @Test
    fun `findByOrigineAndDestination maps entities`() {
        val now = LocalDateTime.now()
        val entity = VolEntity(
            UUID.randomUUID(), "AF5000", "CDG", "LAX",
            now, now.plusHours(11),
            VolEtat.PREVU, null, null, now, now
        )

        every { repo.findByOrigineAndDestination("CDG", "LAX") } returns Flux.just(entity)

        StepVerifier.create(adapter.findByOrigineAndDestination("CDG", "LAX"))
            .expectNextMatches { it.origine == "CDG" && it.destination == "LAX" }
            .verifyComplete()

        verify { repo.findByOrigineAndDestination("CDG", "LAX") }
    }

}
