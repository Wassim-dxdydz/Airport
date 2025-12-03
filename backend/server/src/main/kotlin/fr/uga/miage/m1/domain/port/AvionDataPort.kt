package fr.uga.miage.m1.domain.port

import fr.uga.miage.m1.domain.model.Avion
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface AvionDataPort {

    fun findAll(): Flux<Avion>

    fun findById(id: UUID): Mono<Avion>

    fun findByEtat(etat: AvionEtat): Flux<Avion>

    fun save(avion: Avion): Mono<Avion>

    fun deleteById(id: UUID): Mono<Unit>

    fun existsByImmatriculation(immatriculation: String): Mono<Boolean>


    fun findByImmatriculation(immatriculation: String): Mono<Avion>

}

