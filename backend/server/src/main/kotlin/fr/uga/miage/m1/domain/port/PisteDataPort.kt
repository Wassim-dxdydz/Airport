package fr.uga.miage.m1.domain.port

import fr.uga.miage.m1.domain.model.Piste
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface PisteDataPort {

    fun findAll(): Flux<Piste>

    fun findById(id: UUID): Mono<Piste>

    fun save(piste: Piste): Mono<Piste>

    fun deleteById(id: UUID): Mono<Unit>

    fun findByEtat(etat: PisteEtat): Flux<Piste>

    fun existsById(id: UUID): Mono<Boolean>
}
