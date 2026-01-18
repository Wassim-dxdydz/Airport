package fr.uga.miage.m1.domain.port

import fr.uga.miage.m1.domain.model.Passenger
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface PassengerDataPort {
    fun findAll(): Flux<Passenger>
    fun findById(id: UUID): Mono<Passenger>
    fun save(passenger: Passenger): Mono<Passenger>
    fun deleteById(id: UUID): Mono<Void>
    fun existsById(id: UUID): Mono<Boolean>
    fun existsByEmail(email: String): Mono<Boolean>
}
