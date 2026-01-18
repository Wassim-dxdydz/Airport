package fr.uga.miage.m1.persistence.adapter

import fr.uga.miage.m1.domain.model.CheckIn
import fr.uga.miage.m1.domain.port.CheckInDataPort
import fr.uga.miage.m1.persistence.entity.CheckInEntity
import fr.uga.miage.m1.persistence.repository.CheckInRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class CheckInAdapter(
    private val repository: CheckInRepository
) : CheckInDataPort {

    override fun findAll(): Flux<CheckIn> =
        repository.findAll().map { it.toDomain() }

    override fun findById(id: UUID): Mono<CheckIn> =
        repository.findById(id).map { it.toDomain() }

    override fun findByVolId(volId: UUID): Flux<CheckIn> =
        repository.findByVolId(volId).map { it.toDomain() }

    override fun findByPassagerId(passagerId: UUID): Flux<CheckIn> =
        repository.findByPassagerId(passagerId).map { it.toDomain() }

    override fun save(checkIn: CheckIn): Mono<CheckIn> =
        repository.save(checkIn.toEntity()).map { it.toDomain() }

    override fun deleteById(id: UUID): Mono<Void> =
        repository.deleteById(id)

    override fun existsById(id: UUID): Mono<Boolean> =
        repository.existsById(id)

    override fun existsByVolIdAndNumeroSiege(volId: UUID, numeroSiege: String): Mono<Boolean> =
        repository.existsByVolIdAndNumeroSiege(volId, numeroSiege)

    override fun existsByVolIdAndPassagerId(volId: UUID, passagerId: UUID): Mono<Boolean> =
        repository.existsByVolIdAndPassagerId(volId, passagerId)

    private fun CheckInEntity.toDomain() = CheckIn(
        id = this.id,
        passagerId = this.passagerId,
        volId = this.volId,
        numeroSiege = this.numeroSiege,
        heureCheckIn = this.heureCheckIn
    )

    private fun CheckIn.toEntity() = CheckInEntity(
        id = this.id,
        passagerId = this.passagerId,
        volId = this.volId,
        numeroSiege = this.numeroSiege,
        heureCheckIn = this.heureCheckIn
    )
}
