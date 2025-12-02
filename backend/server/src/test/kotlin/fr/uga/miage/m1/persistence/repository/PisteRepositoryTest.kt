package fr.uga.miage.m1.persistence.repository

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.persistence.entity.PisteEntity
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
class PisteRepositoryTest(@Autowired private val repo: PisteRepository) {

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
    fun `findByEtat works`() {
        val piste = PisteEntity(
            identifiant = "R1",
            longueurM = 3000,
            etat = PisteEtat.LIBRE
        )

        StepVerifier.create(
            repo.save(piste)
                .flatMap { repo.findByEtat(PisteEtat.LIBRE).next() }
        )
            .expectNextMatches { it.identifiant == "R1" }
            .verifyComplete()
    }
}
