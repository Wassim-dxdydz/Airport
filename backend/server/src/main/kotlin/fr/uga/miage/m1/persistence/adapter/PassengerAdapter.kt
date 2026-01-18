package fr.uga.miage.m1.persistence.adapter

import fr.uga.miage.m1.domain.model.Passenger
import fr.uga.miage.m1.domain.port.PassengerDataPort
import fr.uga.miage.m1.persistence.entity.PassengerEntity
import fr.uga.miage.m1.persistence.repository.PassengerRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class PassengerAdapter(
    private val repository: PassengerRepository
) : PassengerDataPort {

    override fun findAll(): Flux<Passenger> =
        repository.findAll().map { it.toDomain() }

    override fun findById(id: UUID): Mono<Passenger> =
        repository.findById(id).map { it.toDomain() }

    override fun save(passenger: Passenger): Mono<Passenger> =
        repository.save(passenger.toEntity()).map { it.toDomain() }

    override fun deleteById(id: UUID): Mono<Void> =
        repository.deleteById(id)

    override fun existsById(id: UUID): Mono<Boolean> =
        repository.existsById(id)

    override fun existsByEmail(email: String): Mono<Boolean> =
        repository.existsByEmail(email)

    private fun PassengerEntity.toDomain() = Passenger(
        id = this.id,
        prenom = this.prenom,
        nom = this.nom,
        email = this.email,
        telephone = this.telephone
    )

    private fun Passenger.toEntity() = PassengerEntity(
        id = this.id,
        prenom = this.prenom,
        nom = this.nom,
        email = this.email,
        telephone = this.telephone
    )
}