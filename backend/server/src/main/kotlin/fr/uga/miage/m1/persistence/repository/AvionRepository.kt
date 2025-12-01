package fr.uga.miage.m1.persistence.repository

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.persistence.entity.AvionEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface AvionRepository : ReactiveCrudRepository<AvionEntity, UUID> {
    fun findByEtat(etat: AvionEtat): Flux<AvionEntity>
    fun existsByImmatriculation(immatriculation: String): Mono<Boolean>
    fun findByImmatriculation(immatriculation: String): Mono<AvionEntity>
    fun findByHangarId(hangarId: UUID): Flux<AvionEntity>
}
