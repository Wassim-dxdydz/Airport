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
import java.util.UUID

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
            etat = AvionEtat.EN_VOL,
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
            capacite = 180, etat = AvionEtat.EN_VOL, hangarId = null
        )
        val a2 = AvionEntity(
            immatriculation = "F-2222", type = "A330",
            capacite = 250, etat = AvionEtat.EN_VOL, hangarId = null
        )
        val a3 = AvionEntity(
            immatriculation = "F-3333", type = "B737",
            capacite = 200, etat = AvionEtat.MAINTENANCE, hangarId = null
        )

        StepVerifier.create(
            repo.save(a1)
                .then(repo.save(a2))
                .then(repo.save(a3))
                .thenMany(repo.findByEtat(AvionEtat.EN_VOL))
                .collectList()
        )
            .expectNextMatches { it.size == 2 && it.all { a -> a.etat == AvionEtat.EN_VOL } }
            .verifyComplete()
    }

    @Test
    fun `existsByImmatriculation returns true when exists`() {
        val avion = AvionEntity(
            immatriculation = "F-XXXX", type = "A320",
            capacite = 180, etat = AvionEtat.EN_VOL, hangarId = null
        )

        StepVerifier.create(
            repo.save(avion)
                .flatMap { repo.existsByImmatriculation("F-XXXX") }
        )
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `existsByImmatriculation returns false when not exists`() {
        StepVerifier.create(
            repo.existsByImmatriculation("F-NONEXISTENT")
        )
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `findByImmatriculation returns avion when exists`() {
        val avion = AvionEntity(
            immatriculation = "F-ABCD", type = "A350",
            capacite = 300, etat = AvionEtat.EN_VOL, hangarId = null
        )

        StepVerifier.create(
            repo.save(avion)
                .then(repo.findByImmatriculation("F-ABCD"))
        )
            .expectNextMatches {
                it.immatriculation == "F-ABCD" && it.type == "A350" && it.capacite == 300
            }
            .verifyComplete()
    }

    @Test
    fun `findByImmatriculation returns empty when not exists`() {
        StepVerifier.create(
            repo.findByImmatriculation("F-NOTFOUND")
        )
            .verifyComplete()
    }

    @Test
    fun `findByHangarId returns all avions in hangar`() {
        val hangarId = UUID.randomUUID()
        val otherHangarId = UUID.randomUUID()

        val a1 = AvionEntity(
            immatriculation = "F-AAA1", type = "A320",
            capacite = 180, etat = AvionEtat.MAINTENANCE, hangarId = hangarId
        )
        val a2 = AvionEntity(
            immatriculation = "F-AAA2", type = "A330",
            capacite = 250, etat = AvionEtat.MAINTENANCE, hangarId = hangarId
        )
        val a3 = AvionEntity(
            immatriculation = "F-BBB1", type = "B777",
            capacite = 400, etat = AvionEtat.MAINTENANCE, hangarId = otherHangarId
        )

        StepVerifier.create(
            repo.save(a1)
                .then(repo.save(a2))
                .then(repo.save(a3))
                .thenMany(repo.findByHangarId(hangarId))
                .collectList()
        )
            .expectNextMatches {
                it.size == 2 && it.all { a -> a.hangarId == hangarId }
            }
            .verifyComplete()
    }

    @Test
    fun `countByHangarId returns zero when hangar is empty`() {
        val emptyHangarId = UUID.randomUUID()

        StepVerifier.create(
            repo.countByHangarId(emptyHangarId)
        )
            .expectNext(0L)
            .verifyComplete()
    }
}
