package fr.uga.miage.m1.repositories

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.models.Hangar
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.test.StepVerifier

class HangarRepositoryTest : RepoBase() {

    @Autowired
    lateinit var repo: HangarRepository

    @Test
    fun `save and find by identifiant`() {
        val h1 = Hangar(identifiant = "H-TEST", capacite = 5, etat = HangarEtat.DISPONIBLE)

        StepVerifier.create(repo.save(h1))
            .expectNextMatches { it.identifiant == "H-TEST" }
            .verifyComplete()

        StepVerifier.create(repo.findByIdentifiant("H-TEST"))
            .expectNextMatches { it.capacite == 5 }
            .verifyComplete()
    }
}
