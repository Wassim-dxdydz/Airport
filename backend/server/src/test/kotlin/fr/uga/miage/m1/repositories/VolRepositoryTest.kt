package fr.uga.miage.m1.repositories

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.models.Vol
import fr.uga.miage.m1.models.Avion
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("test")
class VolRepositoryTest : RepoBase() {

    @Autowired
    lateinit var repo: VolRepository

    @Autowired
    lateinit var avionRepository: AvionRepository

    @BeforeEach
    fun cleanup() {
        repo.deleteAll().block()
        avionRepository.deleteAll().block()
    }

    @Test
    fun `save and find by numeroVol`() {
        // Create avion first with shortened immatriculation
        val avion = avionRepository.save(
            Avion.create(
                immatriculation = "F-TEST-${UUID.randomUUID().toString().take(8)}",
                type = "A320",
                capacite = 180,
                etat = AvionEtat.EN_SERVICE
            )
        ).block()!!

        val numeroVol = "AF1234-${UUID.randomUUID().toString().take(8)}"
        val vol = Vol.create(
            numeroVol = numeroVol,
            avionId = avion.id,
            origine = "LYS",
            destination = "CDG",
            heureDepart = LocalDateTime.now().plusHours(1),
            heureArrivee = LocalDateTime.now().plusHours(3),
            etat = VolEtat.PREVU
        )

        // Test save
        StepVerifier.create(repo.save(vol))
            .expectNextMatches {
                it.numeroVol == vol.numeroVol &&
                        it.avionId == avion.id &&
                        it.origine == "LYS" &&
                        it.destination == "CDG" &&
                        it.etat == VolEtat.PREVU
            }
            .verifyComplete()

        // Test find by numeroVol
        StepVerifier.create(repo.findByNumeroVol(numeroVol))
            .expectNextMatches {
                it.etat == VolEtat.PREVU &&
                        it.origine == "LYS" &&
                        it.destination == "CDG" &&
                        it.numeroVol == numeroVol &&
                        it.avionId == avion.id
            }
            .verifyComplete()
    }
}