package fr.uga.miage.m1.config

import fr.uga.miage.m1.persistence.repository.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier

@Testcontainers
@DataR2dbcTest
@Import(DataInitializer::class)
@ActiveProfiles("test")
class DataInitializerTest(
    @Autowired val hangarRepo: HangarRepository,
    @Autowired val avionRepo: AvionRepository,
    @Autowired val pisteRepo: PisteRepository,
    @Autowired val volRepo: VolRepository,
    @Autowired val volHistoryRepo: VolHistoryRepository,
    @Autowired val initializer: DataInitializer
) {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:17")
    }

    @BeforeEach
    fun setup() {
        initializer.initDatabaseReactive(
            hangarRepo, avionRepo, pisteRepo, volRepo, volHistoryRepo
        ).block()
    }

    @Test
    fun `database should be initialized with expected counts`() {
        StepVerifier.create(hangarRepo.count())
            .expectNext(8)
            .verifyComplete()

        StepVerifier.create(avionRepo.count())
            .expectNext(15)
            .verifyComplete()

        StepVerifier.create(pisteRepo.count())
            .expectNext(5)
            .verifyComplete()

        StepVerifier.create(volRepo.count())
            .expectNext(16)
            .verifyComplete()

        StepVerifier.create(volHistoryRepo.count())
            .expectNext(9)
            .verifyComplete()
    }
}
