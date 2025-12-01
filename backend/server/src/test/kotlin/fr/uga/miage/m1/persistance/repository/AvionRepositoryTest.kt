package fr.uga.miage.m1.persistence.repository

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.persistence.entity.AvionEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.containers.PostgreSQLContainer
import reactor.test.StepVerifier

@Testcontainers
@DataR2dbcTest
@ActiveProfiles("test")
class AvionRepositoryTest(@Autowired private val repo: AvionRepository) {

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
        val avion = AvionEntity(
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = null
        )

        StepVerifier.create(
            repo.save(avion)
                .flatMap { repo.findById(it.id!!) }
        )
            .expectNextMatches { it.immatriculation == "F-GRNB" }
            .verifyComplete()
    }

    @Test
    fun `findByEtat returns matching rows`() {
        val a1 = AvionEntity(
            immatriculation = "F-1111", type = "A320",
            capacite = 180, etat = AvionEtat.EN_SERVICE, hangarId = null
        )
        val a2 = AvionEntity(
            immatriculation = "F-2222", type = "A330",
            capacite = 250, etat = AvionEtat.EN_SERVICE, hangarId = null
        )

        StepVerifier.create(
            repo.save(a1)
                .then(repo.save(a2))
                .thenMany(repo.findByEtat(AvionEtat.EN_SERVICE))
                .collectList()
        )
            .expectNextMatches { it.size == 2 }
            .verifyComplete()
    }

    @Test
    fun `existsByImmatriculation works`() {
        val avion = AvionEntity(
            immatriculation = "F-XXXX", type = "A320",
            capacite = 180, etat = AvionEtat.EN_SERVICE, hangarId = null
        )

        StepVerifier.create(
            repo.save(avion)
                .flatMap { repo.existsByImmatriculation("F-XXXX") }
        )
            .expectNext(true)
            .verifyComplete()
    }
}
