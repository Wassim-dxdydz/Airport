package fr.uga.miage.m1.persistence.repository

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.persistence.entity.VolEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface VolRepository : ReactiveCrudRepository<VolEntity, UUID> {
    fun findByNumeroVol(numeroVol: String): Mono<VolEntity>
    fun findByEtat(etat: VolEtat): Flux<VolEntity>
    fun findByAvionId(avionId: UUID): Flux<VolEntity>
    fun deleteByNumeroVol(numeroVol: String): Mono<Unit>
    fun findByOrigine(origine: String): Flux<VolEntity>
    fun findByDestination(destination: String): Flux<VolEntity>
    fun findByOrigineAndDestination(origine: String, destination: String): Flux<VolEntity>

    fun findByPisteId(pisteId: UUID): Flux<VolEntity>

}
