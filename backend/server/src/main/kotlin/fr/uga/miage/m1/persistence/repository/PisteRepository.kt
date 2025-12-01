package fr.uga.miage.m1.persistence.repository

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.persistence.entity.PisteEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

interface PisteRepository : ReactiveCrudRepository<PisteEntity, UUID> {
    fun findByEtat(etat: PisteEtat): Flux<PisteEntity>
}
