package fr.uga.miage.m1.integration

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.r2dbc.pool.enabled=false",
        "spring.flyway.enabled=true",
        "spring.flyway.clean-disabled=false" // Permet le nettoyage pour les tests
    ]
)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = [
    "spring.sql.init.schema-locations=classpath:schema.sql"
])
abstract class BaseIntegration {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")

        private var r2dbcUrl: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // Démarrer le container manuellement si nécessaire
            if (!postgres.isRunning) {
                postgres.start()
            }

            r2dbcUrl = "r2dbc:postgresql://${postgres.host}:${postgres.getMappedPort(5432)}/${postgres.databaseName}"

            registry.add("spring.r2dbc.url") { r2dbcUrl }
            registry.add("spring.r2dbc.username") { postgres.username }
            registry.add("spring.r2dbc.password") { postgres.password }
            registry.add("spring.flyway.url") { postgres.jdbcUrl }
            registry.add("spring.flyway.user") { postgres.username }
            registry.add("spring.flyway.password") { postgres.password }
        }
    }

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var client: WebTestClient
}