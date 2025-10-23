package fr.uga.miage.m1.repositories

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.models.Piste
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import reactor.test.StepVerifier
import java.util.*

@ActiveProfiles("test")
class PisteRepositoryTest : RepoBase() {

    @Autowired
    lateinit var repo: PisteRepository

    @BeforeEach
    fun cleanup() {
        // Clean all data before each test
        repo.deleteAll().block()
    }

    @Test
    fun `save and find by etat`() {
        val piste = Piste.create(
            identifiant = "R1-${UUID.randomUUID()}",
            longueurM = 3000,
            etat = PisteEtat.LIBRE
        )

        StepVerifier.create(repo.save(piste))
            .expectNextMatches {
                it.identifiant == piste.identifiant &&
                        it.longueurM == 3000
            }
            .verifyComplete()

        StepVerifier.create(repo.findByEtat(PisteEtat.LIBRE))
            .expectNextMatches {
                it.identifiant == piste.identifiant &&
                        it.longueurM == 3000
            }
            .verifyComplete()
    }
}