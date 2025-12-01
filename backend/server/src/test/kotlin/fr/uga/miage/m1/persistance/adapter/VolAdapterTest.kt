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
            avionId = null
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
}
