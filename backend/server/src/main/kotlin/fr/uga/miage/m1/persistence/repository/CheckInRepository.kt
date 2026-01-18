package fr.uga.miage.m1.persistence.repository

import fr.uga.miage.m1.persistence.entity.CheckInEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface CheckInRepository : R2dbcRepository<CheckInEntity, UUID> {
    fun findByVolId(volId: UUID): Flux<CheckInEntity>
    fun findByPassagerId(passagerId: UUID): Flux<CheckInEntity>
    fun existsByVolIdAndNumeroSiege(volId: UUID, numeroSiege: String): Mono<Boolean>
    fun existsByVolIdAndPassagerId(volId: UUID, passagerId: UUID): Mono<Boolean>
}
