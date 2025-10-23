package fr.uga.miage.m1.repositories

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import reactor.test.StepVerifier

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConnectionTest {

    @Autowired
    lateinit var databaseClient: DatabaseClient

    @Test
    fun `test database connection`() {
        StepVerifier.create(
            databaseClient.sql("SELECT 1").fetch().one()
        )
            .expectNextCount(1)
            .verifyComplete()
    }
}