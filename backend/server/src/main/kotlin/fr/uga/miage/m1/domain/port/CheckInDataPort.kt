package fr.uga.miage.m1.domain.port

import fr.uga.miage.m1.domain.model.CheckIn
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface CheckInDataPort {
    fun findAll(): Flux<CheckIn>
    fun findById(id: UUID): Mono<CheckIn>
    fun findByVolId(volId: UUID): Flux<CheckIn>
    fun findByPassagerId(passagerId: UUID): Flux<CheckIn>
    fun save(checkIn: CheckIn): Mono<CheckIn>
    fun deleteById(id: UUID): Mono<Void>
    fun existsById(id: UUID): Mono<Boolean>
    fun existsByVolIdAndNumeroSiege(volId: UUID, numeroSiege: String): Mono<Boolean>
    fun existsByVolIdAndPassagerId(volId: UUID, passagerId: UUID): Mono<Boolean>
}
