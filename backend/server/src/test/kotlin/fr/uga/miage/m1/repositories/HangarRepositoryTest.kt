package fr.uga.miage.m1.repositories

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.models.Hangar
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import reactor.test.StepVerifier
import java.util.*

@ActiveProfiles("test")
class HangarRepositoryTest : RepoBase() {

    @Autowired
    lateinit var repo: HangarRepository

    @BeforeEach
    fun cleanup() {
        repo.deleteAll().block()
    }

    @Test
    fun `save and find by identifiant`() {
        val hangar = Hangar.create(
            identifiant = "H-TEST-${UUID.randomUUID()}",
            capacite = 5,
            etat = HangarEtat.DISPONIBLE
        )

        StepVerifier.create(repo.save(hangar))
            .expectNextMatches {
                it.identifiant == hangar.identifiant &&
                        it.capacite == 5
            }
            .verifyComplete()

        StepVerifier.create(repo.findByIdentifiant(hangar.identifiant))
            .expectNextMatches {
                it.capacite == 5 &&
                        it.etat == HangarEtat.DISPONIBLE
            }
            .verifyComplete()
    }
}