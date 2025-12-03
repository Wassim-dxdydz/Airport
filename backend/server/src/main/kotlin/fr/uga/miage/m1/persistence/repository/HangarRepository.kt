package fr.uga.miage.m1.persistence.repository

import fr.uga.miage.m1.persistence.entity.HangarEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID

interface HangarRepository : ReactiveCrudRepository<HangarEntity, UUID> {
    fun findByIdentifiant(identifiant: String): Mono<HangarEntity>
    fun deleteByIdentifiant(identifiant: String): Mono<Unit>
}
