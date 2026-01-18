package fr.uga.miage.m1.persistence.repository

import fr.uga.miage.m1.persistence.entity.PassengerEntity
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
class PassengerRepositoryTest(@Autowired private val repo: PassengerRepository) {

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
        val passenger = PassengerEntity(
            nom = "Dupont",
            prenom = "Jean",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )

        StepVerifier.create(
            repo.save(passenger)
                .flatMap { repo.findById(it.id!!) }
        )
            .expectNextMatches {
                it.nom == "Dupont" && it.email == "jean.dupont@example.com"
            }
            .verifyComplete()
    }

    @Test
    fun `existsByEmail returns true when email exists`() {
        val passenger = PassengerEntity(
            nom = "Martin",
            prenom = "Sophie",
            email = "sophie.martin@example.com",
            telephone = "+33687654321"
        )

        StepVerifier.create(
            repo.save(passenger)
                .flatMap { repo.existsByEmail("sophie.martin@example.com") }
        )
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `existsByEmail returns false when email does not exist`() {
        StepVerifier.create(
            repo.existsByEmail("nonexistent@example.com")
        )
            .expectNext(false)
            .verifyComplete()
    }
}
