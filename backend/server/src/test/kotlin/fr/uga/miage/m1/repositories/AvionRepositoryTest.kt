package fr.uga.miage.m1.repositories

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.models.Avion
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import reactor.test.StepVerifier
import java.util.*

@ActiveProfiles("test")
class AvionRepositoryTest : RepoBase() {

    @Autowired
    lateinit var repo: AvionRepository

    @BeforeEach
    fun cleanup() {
        repo.deleteAll().block()
    }

    @Test
    fun `save and find by immatriculation`() {
        val immatriculation = "F-TEST-${UUID.randomUUID().toString().take(8)}"
        val avion = Avion.create(
            immatriculation = immatriculation,
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE
        )

        // Test save
        StepVerifier.create(repo.save(avion))
            .expectNextMatches { saved ->
                saved.immatriculation == immatriculation &&
                        saved.id != null &&
                        saved.type == "A320" &&
                        saved.capacite == 180
            }
            .verifyComplete()

        // Test find by immatriculation
        StepVerifier.create(repo.findByImmatriculation(immatriculation))
            .expectNextMatches { found ->
                found.immatriculation == immatriculation &&
                        found.type == "A320" &&
                        found.capacite == 180 &&
                        found.etat == AvionEtat.EN_SERVICE &&
                        found.id != null
            }
            .verifyComplete()
    }
}