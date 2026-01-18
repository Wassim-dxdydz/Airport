package fr.uga.miage.m1.persistence.adapter

import fr.uga.miage.m1.domain.model.CheckIn
import fr.uga.miage.m1.persistence.entity.CheckInEntity
import fr.uga.miage.m1.persistence.repository.CheckInRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.UUID

class CheckInAdapterTest {

    private lateinit var repository: CheckInRepository
    private lateinit var adapter: CheckInAdapter

    @BeforeEach
    fun setUp() {
        repository = mockk()
        adapter = CheckInAdapter(repository)
    }

    @Test
    fun `findAll maps entities to domain`() {
        val entity = CheckInEntity(
            id = UUID.randomUUID(),
            volId = UUID.randomUUID(),
            passagerId = UUID.randomUUID(),
            numeroSiege = "12A",
            heureCheckIn = LocalDateTime.now()
        )

        every { repository.findAll() } returns Flux.just(entity)

        StepVerifier.create(adapter.findAll())
            .expectNextMatches { it.numeroSiege == "12A" }
            .verifyComplete()

        verify { repository.findAll() }
    }

    @Test
    fun `findById returns mapped domain`() {
        val id = UUID.randomUUID()
        val entity = CheckInEntity(
            id = id,
            volId = UUID.randomUUID(),
            passagerId = UUID.randomUUID(),
            numeroSiege = "15C",
            heureCheckIn = LocalDateTime.now()
        )

        every { repository.findById(id) } returns Mono.just(entity)

        StepVerifier.create(adapter.findById(id))
            .expectNextMatches { it.id == id && it.numeroSiege == "15C" }
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
    fun `findByVolId maps entities to domain`() {
        val volId = UUID.randomUUID()
        val entity1 = CheckInEntity(
            id = UUID.randomUUID(),
            volId = volId,
            passagerId = UUID.randomUUID(),
            numeroSiege = "10A",
            heureCheckIn = LocalDateTime.now()
        )

        every { repository.findByVolId(volId) } returns Flux.just(entity1)

        StepVerifier.create(adapter.findByVolId(volId))
            .expectNextMatches { it.volId == volId && it.numeroSiege == "10A" }
            .verifyComplete()

        verify { repository.findByVolId(volId) }
    }

    @Test
    fun `findByPassagerId maps entities to domain`() {
        val passagerId = UUID.randomUUID()
        val entity = CheckInEntity(
            id = UUID.randomUUID(),
            volId = UUID.randomUUID(),
            passagerId = passagerId,
            numeroSiege = "8B",
            heureCheckIn = LocalDateTime.now()
        )

        every { repository.findByPassagerId(passagerId) } returns Flux.just(entity)

        StepVerifier.create(adapter.findByPassagerId(passagerId))
            .expectNextMatches { it.passagerId == passagerId && it.numeroSiege == "8B" }
            .verifyComplete()

        verify { repository.findByPassagerId(passagerId) }
    }

    @Test
    fun `save maps domain to entity and back`() {
        val id = UUID.randomUUID()
        val volId = UUID.randomUUID()
        val passagerId = UUID.randomUUID()
        val heureCheckIn = LocalDateTime.now()

        val domain = CheckIn(
            id = id,
            volId = volId,
            passagerId = passagerId,
            numeroSiege = "20A",
            heureCheckIn = heureCheckIn
        )

        val entity = CheckInEntity(
            id = id,
            volId = volId,
            passagerId = passagerId,
            numeroSiege = "20A",
            heureCheckIn = heureCheckIn
        )

        every { repository.save(any()) } returns Mono.just(entity)

        StepVerifier.create(adapter.save(domain))
            .expectNextMatches { it.id == id && it.numeroSiege == "20A" }
            .verifyComplete()

        verify { repository.save(match { it.numeroSiege == "20A" }) }
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
    fun `existsByVolIdAndNumeroSiege delegates to repository`() {
        val volId = UUID.randomUUID()
        every { repository.existsByVolIdAndNumeroSiege(volId, "12A") } returns Mono.just(true)

        StepVerifier.create(adapter.existsByVolIdAndNumeroSiege(volId, "12A"))
            .expectNext(true)
            .verifyComplete()

        verify { repository.existsByVolIdAndNumeroSiege(volId, "12A") }
    }

    @Test
    fun `existsByVolIdAndPassagerId delegates to repository`() {
        val volId = UUID.randomUUID()
        val passagerId = UUID.randomUUID()
        every { repository.existsByVolIdAndPassagerId(volId, passagerId) } returns Mono.just(false)

        StepVerifier.create(adapter.existsByVolIdAndPassagerId(volId, passagerId))
            .expectNext(false)
            .verifyComplete()

        verify { repository.existsByVolIdAndPassagerId(volId, passagerId) }
    }
}
