package fr.uga.miage.m1.services

import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteEtatRequest
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.models.Piste
import fr.uga.miage.m1.repositories.PisteRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class PisteServiceTest {

    private lateinit var repo: PisteRepository
    private lateinit var service: PisteService

    @BeforeEach
    fun setUp() {
        repo = mockk()
        service = PisteService(repo)
    }

    @Test
    fun `create piste default libre`() {
        val req = CreatePisteRequest(identifiant = "R1", longueurM = 3200, etat = PisteEtat.LIBRE)
        val saved = Piste(identifiant = "R1", longueurM = 3200, etat = PisteEtat.LIBRE)
        every { repo.save(any()) } returns Mono.just(saved)

        StepVerifier.create(service.create(req))
            .expectNext(saved)
            .verifyComplete()
    }

    @Test
    fun `update etat to OCCUPEE`() {
        val id = UUID.randomUUID()
        val current = Piste(id, "R1", 3000, PisteEtat.LIBRE)
        val req = UpdatePisteEtatRequest(PisteEtat.OCCUPEE)

        every { repo.findById(id) } returns Mono.just(current)
        every { repo.save(any()) } returns Mono.just(current.copy(etat = PisteEtat.OCCUPEE))

        StepVerifier.create(service.updateEtat(id, req))
            .expectNextMatches { it.etat == PisteEtat.OCCUPEE }
            .verifyComplete()
    }

    @Test
    fun `disponibles returns only LIBRE`() {
        val r1 = Piste(UUID.randomUUID(), "R1", 3000, PisteEtat.LIBRE)
        every { repo.findByEtat(PisteEtat.LIBRE) } returns Flux.just(r1)

        StepVerifier.create(service.disponibles())
            .expectNext(r1)
            .verifyComplete()

        verify { repo.findByEtat(PisteEtat.LIBRE) }
    }
}
