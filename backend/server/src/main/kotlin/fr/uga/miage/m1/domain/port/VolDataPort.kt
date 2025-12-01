package fr.uga.miage.m1.domain.port

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Vol
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface VolDataPort {

    fun findAll(): Flux<Vol>

    fun findById(id: UUID): Mono<Vol>

    fun save(vol: Vol): Mono<Vol>

    fun deleteById(id: UUID): Mono<Void>

    fun findByNumeroVol(numeroVol: String): Mono<Vol>

    fun deleteByNumeroVol(numeroVol: String): Mono<Void>

    fun findByEtat(etat: VolEtat): Flux<Vol>

    fun findByAvionId(avionId: UUID): Flux<Vol>

    fun findByOrigine(origine: String): Flux<Vol>

    fun findByDestination(destination: String): Flux<Vol>

    fun findByOrigineAndDestination(origine: String, destination: String): Flux<Vol>
}
