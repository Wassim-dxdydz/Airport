package fr.uga.miage.m1.repositories

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.models.Avion
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.test.StepVerifier

class AvionRepositoryTest : RepoBase() {

    @Autowired lateinit var repo: AvionRepository

    @Test
    fun `save and find by immatriculation and etat`() {
        val a = Avion(immatriculation="F-R2DBC", type="A320", capacite=180, etat=AvionEtat.EN_SERVICE)

        StepVerifier.create(repo.save(a))
            .expectNextMatches { it.immatriculation == "F-R2DBC" }
            .verifyComplete()

        StepVerifier.create(repo.findByImmatriculation("F-R2DBC"))
            .expectNextMatches { it.type == "A320" }
            .verifyComplete()

        StepVerifier.create(repo.findByEtat(AvionEtat.EN_SERVICE))
            .expectNextCount(1)
            .verifyComplete()
    }
}
