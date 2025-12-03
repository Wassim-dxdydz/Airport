package fr.uga.miage.m1.domain.port

import fr.uga.miage.m1.domain.model.Hangar
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface HangarDataPort {

    fun findAll(): Flux<Hangar>

    fun findById(id: UUID): Mono<Hangar>

    fun findByIdentifiant(identifiant: String): Mono<Hangar>

    fun save(hangar: Hangar): Mono<Hangar>

    fun deleteById(id: UUID): Mono<Unit>

    fun deleteByIdentifiant(identifiant: String): Mono<Unit>

    fun existsById(id: UUID): Mono<Boolean>
}
