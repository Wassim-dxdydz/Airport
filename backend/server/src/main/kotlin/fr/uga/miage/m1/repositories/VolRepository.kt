package fr.uga.miage.m1.repositories

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.models.Vol
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface VolRepository : ReactiveCrudRepository<Vol, UUID> {
    fun findByNumeroVol(numeroVol: String): Mono<Vol>
    fun findByEtat(etat: VolEtat): Flux<Vol>
    fun findByAvionId(avionId: UUID): Flux<Vol>
}
