package fr.uga.miage.m1.persistence.adapter

import fr.uga.miage.m1.domain.model.Passenger
import fr.uga.miage.m1.persistence.entity.PassengerEntity
import fr.uga.miage.m1.persistence.repository.PassengerRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

class PassengerAdapterTest {

    private lateinit var repository: PassengerRepository
    private lateinit var adapter: PassengerAdapter

    @BeforeEach
    fun setUp() {
        repository = mockk()
        adapter = PassengerAdapter(repository)
    }

    @Test
    fun `findAll maps entities to domain`() {
        val entity = PassengerEntity(
            id = UUID.randomUUID(),
            nom = "Dupont",
            prenom = "Jean",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )

        every { repository.findAll() } returns Flux.just(entity)

        StepVerifier.create(adapter.findAll())
            .expectNextMatches { it.nom == "Dupont" && it.email == "jean.dupont@example.com" }
            .verifyComplete()

        verify { repository.findAll() }
    }

    @Test
    fun `findById returns mapped domain`() {
        val id = UUID.randomUUID()
        val entity = PassengerEntity(
            id = id,
            nom = "Martin",
            prenom = "Sophie",
            email = "sophie.martin@example.com",
            telephone = "+33687654321"
        )

        every { repository.findById(id) } returns Mono.just(entity)

        StepVerifier.create(adapter.findById(id))
            .expectNextMatches { it.id == id && it.nom == "Martin" }
            .verifyComplete()

        verify { repository.findById(id) }
    }

    @Test
    fun `findById returns empty when not found`() {
        val id = UUID.randomUUID()
        every { repository.findById(id) } returns Mono.empty()

        StepVerifier.create(adapter.findById(id))
            .verifyComplete()
    }

    @Test
    fun `save maps domain to entity and back`() {
        val id = UUID.randomUUID()
        val domain = Passenger(
            id = id,
            nom = "Lefebvre",
            prenom = "Pierre",
            email = "pierre.lefebvre@example.com",
            telephone = "+33698765432"
        )
        val entity = PassengerEntity(
            id = id,
            nom = "Lefebvre",
            prenom = "Pierre",
            email = "pierre.lefebvre@example.com",
            telephone = "+33698765432"
        )

        every { repository.save(any()) } returns Mono.just(entity)

        StepVerifier.create(adapter.save(domain))
            .expectNextMatches {
                it.id == id && it.nom == "Lefebvre" && it.email == "pierre.lefebvre@example.com"
            }
            .verifyComplete()

        verify { repository.save(match { it.nom == "Lefebvre" && it.prenom == "Pierre" }) }
    }

    @Test
    fun `deleteById delegates to repository`() {
        val id = UUID.randomUUID()
        every { repository.deleteById(id) } returns Mono.empty()

        StepVerifier.create(adapter.deleteById(id))
            .verifyComplete()

        verify { repository.deleteById(id) }
    }

    @Test
    fun `existsByEmail returns true when exists`() {
        val email = "test@example.com"
        every { repository.existsByEmail(email) } returns Mono.just(true)

        StepVerifier.create(adapter.existsByEmail(email))
            .expectNext(true)
            .verifyComplete()

        verify { repository.existsByEmail(email) }
    }

    @Test
    fun `existsByEmail returns false when not exists`() {
        val email = "nonexistent@example.com"
        every { repository.existsByEmail(email) } returns Mono.just(false)

        StepVerifier.create(adapter.existsByEmail(email))
            .expectNext(false)
            .verifyComplete()

        verify { repository.existsByEmail(email) }
    }
}
