package fr.uga.miage.m1.services

import fr.uga.miage.m1.requests.CreateAvionRequest
import fr.uga.miage.m1.requests.UpdateAvionRequest
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.models.Avion
import fr.uga.miage.m1.repositories.AvionRepository
import fr.uga.miage.m1.repositories.HangarRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class AvionServiceTest {

    private lateinit var repo: AvionRepository
    private lateinit var hangars: HangarRepository
    private lateinit var service: AvionService

    @BeforeEach
    fun setUp() {
        repo = mockk()
        hangars = mockk()
        service = AvionService(repo, hangars)
    }

    @Test
    fun `create saves entity`() {
        val req = CreateAvionRequest(
            immatriculation = "F-ABCD",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = null
        )
        val saved = Avion(immatriculation = "F-ABCD", type = "A320", capacite = 180, etat = AvionEtat.EN_SERVICE)

        every { repo.save(any()) } returns Mono.just(saved)

        StepVerifier.create(service.create(req))
            .expectNext(saved)
            .verifyComplete()

        verify { repo.save(match { it.immatriculation == "F-ABCD" && it.hangarId == null }) }
        confirmVerified(repo)
    }

    @Test
    fun `get missing - NotFoundException`() {
        val id = UUID.randomUUID()
        every { repo.findById(id) } returns Mono.empty()

        StepVerifier.create(service.get(id))
            .expectErrorSatisfies { ex ->
                assert(ex is NotFoundException)
                assert(ex.message!!.contains(id.toString()))
            }
            .verify()
    }

    @Test
    fun `update merges nullable fields`() {
        val id = UUID.randomUUID()
        val existing = Avion(id, "F-ABCD", "A320", 180, AvionEtat.EN_SERVICE)
        val req = UpdateAvionRequest(type = "A321", capacite = null, etat = null, hangarId = null)

        every { repo.findById(id) } returns Mono.just(existing)
        every { repo.save(any()) } returns Mono.just(existing.copy(type = "A321"))

        StepVerifier.create(service.update(id, req))
            .expectNextMatches { it.type == "A321" && it.capacite == 180 }
            .verifyComplete()

        verify { repo.save(match { it.type == "A321" && it.capacite == 180 }) }
    }

    @Test
    fun `assignHangar - error when hangar not found`() {
        val avionId = UUID.randomUUID()
        val hangarId = UUID.randomUUID()
        every { hangars.existsById(hangarId) } returns Mono.just(false)

        StepVerifier.create(service.assignHangar(avionId, hangarId))
            .expectErrorSatisfies { ex -> assert(ex is NotFoundException) }
            .verify()
    }
}
