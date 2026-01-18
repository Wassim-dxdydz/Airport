package fr.uga.miage.m1.persistence.repository

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.persistence.entity.HangarEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier

@Testcontainers
@DataR2dbcTest
@ActiveProfiles("test")
class HangarRepositoryTest(@Autowired private val repo: HangarRepository) {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:17")
    }

    @BeforeEach
    fun cleanup() {
        repo.deleteAll().block()
    }

    @Test
    fun `save and findById`() {
        val hangar = HangarEntity(
            identifiant = "H1",
            capacite = 10,
            etat = HangarEtat.DISPONIBLE
        )

        StepVerifier.create(
            repo.save(hangar)
                .flatMap { repo.findById(it.id!!) }
        )
            .expectNextMatches { it.identifiant == "H1" && it.capacite == 10 }
            .verifyComplete()
    }

    @Test
    fun `findByIdentifiant returns hangar when exists`() {
        val hangar = HangarEntity(
            identifiant = "H2",
            capacite = 15,
            etat = HangarEtat.DISPONIBLE
        )

        StepVerifier.create(
            repo.save(hangar)
                .then(repo.findByIdentifiant("H2"))
        )
            .expectNextMatches { it.identifiant == "H2" }
            .verifyComplete()
    }

    @Test
    fun `findByIdentifiant returns empty when not exists`() {
        StepVerifier.create(
            repo.findByIdentifiant("NONEXISTENT")
        )
            .verifyComplete()
    }

    @Test
    fun `deleteByIdentifiant removes hangar`() {
        val hangar = HangarEntity(
            identifiant = "H3",
            capacite = 8,
            etat = HangarEtat.MAINTENANCE
        )

        StepVerifier.create(
            repo.save(hangar)
                .then(repo.deleteByIdentifiant("H3"))
                .then(repo.findByIdentifiant("H3"))
        )
            .verifyComplete()
    }

    @Test
    fun `existsById returns true when hangar exists`() {
        val hangar = HangarEntity(
            identifiant = "HX",
            capacite = 5,
            etat = HangarEtat.DISPONIBLE
        )

        StepVerifier.create(
            repo.save(hangar)
                .flatMap { repo.existsById(it.id!!) }
        )
            .expectNext(true)
            .verifyComplete()
    }
}
