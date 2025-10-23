package fr.uga.miage.m1.repositories

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.models.Piste
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface PisteRepository : ReactiveCrudRepository<Piste, UUID> {
    fun findByEtat(etat: PisteEtat): Flux<Piste>
    fun deleteByIdentifiant(identifiant: String): Mono<Void>
}
