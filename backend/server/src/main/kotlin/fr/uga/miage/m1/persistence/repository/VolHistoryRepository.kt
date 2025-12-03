package fr.uga.miage.m1.persistence.repository

import fr.uga.miage.m1.persistence.entity.VolHistoryEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

interface VolHistoryRepository : ReactiveCrudRepository<VolHistoryEntity, UUID> {
    fun findByVolId(volId: UUID): Flux<VolHistoryEntity>
}
