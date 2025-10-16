package fr.uga.miage.m1.repositories

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.models.Piste
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.test.StepVerifier

class PisteRepositoryTest : RepoBase() {

    @Autowired
    lateinit var repo: PisteRepository

    @Test
    fun `save and find by etat`() {
        val p1 = Piste(identifiant = "R1", longueurM = 3000, etat = PisteEtat.LIBRE)

        StepVerifier.create(repo.save(p1))
            .expectNextMatches { it.identifiant == "R1" }
            .verifyComplete()

        StepVerifier.create(repo.findByEtat(PisteEtat.LIBRE))
            .expectNextMatches { it.identifiant == "R1" }
            .verifyComplete()
    }
}
