package fr.uga.miage.m1.persistence.repository

import fr.uga.miage.m1.persistence.entity.PassengerEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface PassengerRepository : R2dbcRepository<PassengerEntity, UUID> {
    fun existsByEmail(email: String): Mono<Boolean>
}
