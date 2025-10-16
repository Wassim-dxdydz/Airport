package fr.uga.miage.m1.services

import fr.uga.miage.m1.requests.CreateHangarRequest
import fr.uga.miage.m1.requests.UpdateHangarRequest
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.models.Hangar
import fr.uga.miage.m1.repositories.AvionRepository
import fr.uga.miage.m1.repositories.HangarRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class HangarServiceTest {

    private lateinit var repo: HangarRepository
    private lateinit var avions: AvionRepository
    private lateinit var service: HangarService

    @BeforeEach
    fun setUp() {
        repo = mockk()
        avions = mockk()
        service = HangarService(repo, avions)
    }

    @Test
    fun `listAvions returns mapped list`() {
        val id = UUID.randomUUID()
        every { avions.findByHangarId(id) } returns Flux.empty()

        StepVerifier.create(service.listAvions(id))
            .verifyComplete()
    }

    @Test
    fun `create hangar`() {
        val req = CreateHangarRequest("H1", 10, HangarEtat.DISPONIBLE)
        val saved = Hangar(identifiant = "H1", capacite = 10, etat = HangarEtat.DISPONIBLE)
        every { repo.save(any()) } returns Mono.just(saved)

        StepVerifier.create(service.create(req))
            .expectNext(saved)
            .verifyComplete()
    }

    @Test
    fun `update hangar keeps old values when null`() {
        val id = UUID.randomUUID()
        val current = Hangar(id, "H1", 10, HangarEtat.DISPONIBLE)
        every { repo.findById(id) } returns Mono.just(current)
        every { repo.save(any()) } returns Mono.just(current.copy(capacite = 12))

        StepVerifier.create(service.update(id, UpdateHangarRequest(capacite = 12, etat = null)))
            .expectNextMatches { it.capacite == 12 && it.etat == HangarEtat.DISPONIBLE }
            .verifyComplete()
    }
}
