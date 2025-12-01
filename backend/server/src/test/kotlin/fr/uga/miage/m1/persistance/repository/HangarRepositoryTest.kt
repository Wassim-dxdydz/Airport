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
    fun `save and findByIdentifiant`() {
        val hangar = HangarEntity(
            identifiant = "H1",
            capacite = 10,
            etat = HangarEtat.DISPONIBLE
        )

        StepVerifier.create(
            repo.save(hangar)
                .flatMap { repo.findByIdentifiant("H1") }
        )
            .expectNextMatches { it.identifiant == "H1" }
            .verifyComplete()
    }

    @Test
    fun `existsById returns true`() {
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
