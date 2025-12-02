package fr.uga.miage.m1.persistence.repository

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.persistence.entity.VolEntity
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
import java.time.LocalDateTime

@Testcontainers
@DataR2dbcTest
@ActiveProfiles("test")
class VolRepositoryTest(@Autowired private val repo: VolRepository) {

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
    fun `save and findByNumeroVol`() {
        val now = LocalDateTime.now()
        val vol = VolEntity(
            numeroVol = "AF123",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            createdAt = now,
            updatedAt = now
        )

        StepVerifier.create(
            repo.save(vol)
                .flatMap { repo.findByNumeroVol("AF123") }
        )
            .expectNextMatches { it.numeroVol == "AF123" }
            .verifyComplete()
    }

    @Test
    fun `findByEtat returns results`() {
        val now = LocalDateTime.now()
        val vol = VolEntity(
            numeroVol = "AF200",
            origine = "LYS",
            destination = "CDG",
            heureDepart = now,
            heureArrivee = now.plusHours(1),
            etat = VolEtat.EN_ATTENTE,
            avionId = null,
            createdAt = now,
            updatedAt = now
        )

        StepVerifier.create(
            repo.save(vol)
                .flatMap { repo.findByEtat(VolEtat.EN_ATTENTE).collectList() }
        )
            .expectNextMatches { it.isNotEmpty() }
            .verifyComplete()
    }
}
