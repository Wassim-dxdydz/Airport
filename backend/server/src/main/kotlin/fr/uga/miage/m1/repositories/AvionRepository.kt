package fr.uga.miage.m1.repositories

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.models.Avion
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface AvionRepository : ReactiveCrudRepository<Avion, UUID> {
    fun findByImmatriculation(immatriculation: String): Mono<Avion>
    fun findByHangarId(hangarId: UUID): Flux<Avion>
    fun findByEtat(etat: AvionEtat): Flux<Avion>
    fun deleteByImmatriculation(immatriculation: String): Mono<Void>
}
