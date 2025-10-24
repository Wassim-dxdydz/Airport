package fr.uga.miage.m1.services

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.models.Vol
import fr.uga.miage.m1.repositories.AvionRepository
import fr.uga.miage.m1.repositories.VolRepository
import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.requests.UpdateVolRequest
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.UUID

class VolServiceTest {

    private lateinit var repo: VolRepository
    private lateinit var avions: AvionRepository
    private lateinit var strategy: VolStatusStrategy
    private lateinit var service: VolService

    @BeforeEach
    fun setUp() {
        repo = mockk()
        avions = mockk()
        strategy = mockk()
        service = VolService(repo, avions, strategy)
    }

    @Test
    fun `create saves entity`() {
        val now = LocalDateTime.now()
        val req = CreateVolRequest(
            numeroVol = "AF1234",
            origine = "CDG",
            destination = "JFK",
            heureDepart = now.plusHours(2),
            heureArrivee = now.plusHours(8)
        )
        val saved = Vol.create(
            numeroVol = req.numeroVol,
            origine = req.origine,
            destination = req.destination,
            heureDepart = req.heureDepart,
            heureArrivee = req.heureArrivee,
            etat = VolEtat.PREVU,
            avionId = null
        )

        every { repo.save(any<Vol>()) } returns Mono.just(saved)

        StepVerifier.create(service.create(req))
            .expectNext(saved)
            .verifyComplete()

        verify {
            repo.save(match<Vol> {
                it.numeroVol == "AF1234" &&
                        it.origine == "CDG" &&
                        it.destination == "JFK" &&
                        it.etat == VolEtat.PREVU &&
                        it.avionId == null
            })
        }
        confirmVerified(repo)
    }

    @Test
    fun `get missing - NotFoundException`() {
        val id = UUID.randomUUID()
        every { repo.findById(id) } returns Mono.empty()

        // VolService#get comes from BaseCrudService
        StepVerifier.create(service.get(id))
            .expectErrorSatisfies { ex ->
                assert(ex is NotFoundException)
                assert(ex.message!!.contains(id.toString()))
            }
            .verify()

        verify { repo.findById(id) }
        confirmVerified(repo)
    }

    @Test
    fun `update merges nullable fields`() {
        val id = UUID.randomUUID()
        val now = LocalDateTime.now()
        val existing = Vol(
            id = id,
            numeroVol = "AF2000",
            origine = "LYS",
            destination = "CDG",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(3),
            etat = VolEtat.PREVU,
            avionId = null
        )
        val req = UpdateVolRequest(
            origine = "MRS",            // change
            destination = null,         // keep current
            heureDepart = null,         // keep current
            heureArrivee = now.plusHours(4), // change
            etat = null,                // keep current
            avionId = null              // keep current
        )

        every { repo.findById(id) } returns Mono.just(existing)
        every { repo.save(any<Vol>()) } returns Mono.just(
            existing.copy(origine = "MRS", heureArrivee = now.plusHours(4))
        )

        StepVerifier.create(service.update(id, req))
            .expectNextMatches {
                it.origine == "MRS" &&
                        it.destination == "CDG" &&
                        it.heureDepart == existing.heureDepart &&
                        it.heureArrivee == now.plusHours(4) &&
                        it.etat == VolEtat.PREVU
            }
            .verifyComplete()

        verify {
            repo.save(match<Vol> {
                it.origine == "MRS" &&
                        it.destination == "CDG" &&
                        it.heureArrivee == now.plusHours(4) &&
                        it.etat == VolEtat.PREVU
            })
        }
    }

    @Test
    fun `assignAvion - error when avion not found`() {
        val volId = UUID.randomUUID()
        val avionId = UUID.randomUUID()
        every { avions.existsById(avionId) } returns Mono.just(false)

        StepVerifier.create(service.assignAvion(volId, avionId))
            .expectErrorSatisfies { ex -> assert(ex is NotFoundException) }
            .verify()

        verify { avions.existsById(avionId) }
        confirmVerified(avions)
    }

    @Test
    fun `assignAvion - success`() {
        val volId = UUID.randomUUID()
        val avionId = UUID.randomUUID()
        val now = LocalDateTime.now()
        val current = Vol(
            id = volId,
            numeroVol = "AF3000",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(3),
            etat = VolEtat.PREVU,
            avionId = null
        )
        val updated = current.copy(avionId = avionId)

        every { avions.existsById(avionId) } returns Mono.just(true)
        every { repo.findById(volId) } returns Mono.just(current)
        every { repo.save(updated) } returns Mono.just(updated)

        StepVerifier.create(service.assignAvion(volId, avionId))
            .expectNextMatches { it.avionId == avionId }
            .verifyComplete()

        verify { avions.existsById(avionId) }
        verify { repo.findById(volId) }
        verify { repo.save(match { it.avionId == avionId }) }
    }

    @Test
    fun `unassignAvion clears avionId`() {
        val volId = UUID.randomUUID()
        val now = LocalDateTime.now()
        val current = Vol(
            id = volId,
            numeroVol = "AF3001",
            origine = "MAD",
            destination = "CDG",
            heureDepart = now.plusHours(2),
            heureArrivee = now.plusHours(4),
            etat = VolEtat.PREVU,
            avionId = UUID.randomUUID()
        )
        val updated = current.copy(avionId = null)

        every { repo.findById(volId) } returns Mono.just(current)
        every { repo.save(updated) } returns Mono.just(updated)

        StepVerifier.create(service.unassignAvion(volId))
            .expectNextMatches { it.avionId == null }
            .verifyComplete()

        verify { repo.findById(volId) }
        verify { repo.save(match { it.avionId == null }) }
    }

    @Test
    fun `updateEtat - allowed transition saves`() {
        val volId = UUID.randomUUID()
        val now = LocalDateTime.now()
        val current = Vol(
            id = volId,
            numeroVol = "AF4000",
            origine = "CDG",
            destination = "LIS",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(3),
            etat = VolEtat.EN_ATTENTE
        )
        val target = VolEtat.EMBARQUEMENT
        val updated = current.copy(etat = target)

        every { repo.findById(volId) } returns Mono.just(current)
        every { strategy.canTransition(current.etat, target) } returns true
        every { repo.save(updated) } returns Mono.just(updated)

        StepVerifier.create(service.updateEtat(volId, target))
            .expectNextMatches { it.etat == VolEtat.EMBARQUEMENT }
            .verifyComplete()

        verify { strategy.canTransition(VolEtat.EN_ATTENTE, VolEtat.EMBARQUEMENT) }
        verify { repo.save(match { it.etat == VolEtat.EMBARQUEMENT }) }
    }

    @Test
    fun `updateEtat - forbidden transition errors`() {
        val volId = UUID.randomUUID()
        val now = LocalDateTime.now()
        val current = Vol(
            id = volId,
            numeroVol = "AF4001",
            origine = "LIS",
            destination = "CDG",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(3),
            etat = VolEtat.ARRIVE
        )
        val target = VolEtat.DECOLLE

        every { repo.findById(volId) } returns Mono.just(current)
        every { strategy.canTransition(current.etat, target) } returns false

        StepVerifier.create(service.updateEtat(volId, target))
            .expectErrorSatisfies { ex ->
                assert(ex is IllegalArgumentException)
                assert(ex.message!!.contains("non autorisée"))
            }
            .verify()

        verify { strategy.canTransition(VolEtat.ARRIVE, VolEtat.DECOLLE) }
        // repo.save must NOT be called
        verify(exactly = 0) { repo.save(any()) }
    }

    @Test
    fun `listByEtat delegates to repository`() {
        val now = LocalDateTime.now()
        val v1 = Vol(
            numeroVol = "AF7000",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now.plusHours(2),
            heureArrivee = now.plusHours(4),
            etat = VolEtat.EN_VOL
        )
        val v2 = Vol(
            numeroVol = "AF7001",
            origine = "MAD",
            destination = "CDG",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(3),
            etat = VolEtat.EN_VOL
        )

        every { repo.findByEtat(VolEtat.EN_VOL) } returns Flux.just(v1, v2)

        StepVerifier.create(service.listByEtat(VolEtat.EN_VOL))
            .expectNext(v1)
            .expectNext(v2)
            .verifyComplete()

        verify { repo.findByEtat(VolEtat.EN_VOL) }
        confirmVerified(repo)
    }
}
