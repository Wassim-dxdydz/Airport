package fr.uga.miage.m1.persistence.adapter

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.persistence.entity.AvionEntity
import fr.uga.miage.m1.persistence.repository.AvionRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class AvionAdapter(
    private val repo: AvionRepository
) : AvionDataPort {

    override fun findAll(): Flux<Avion> =
        repo.findAll().map { it.toDomain() }

    override fun findById(id: UUID): Mono<Avion> =
        repo.findById(id).map { it.toDomain() }

    override fun findByEtat(etat: AvionEtat): Flux<Avion> =
        repo.findByEtat(etat).map { it.toDomain() }

    override fun save(avion: Avion): Mono<Avion> =
        repo.save(avion.toEntity()).map { it.toDomain() }

    override fun deleteById(id: UUID): Mono<Unit> =
        repo.deleteById(id).thenReturn(Unit)

    override fun existsByImmatriculation(immatriculation: String): Mono<Boolean> =
        repo.existsByImmatriculation(immatriculation)

    override fun findByImmatriculation(immatriculation: String): Mono<Avion> =
        repo.findByImmatriculation(immatriculation).map { it.toDomain() }

}
fun AvionEntity.toDomain() = Avion(
    id = id,
    immatriculation = immatriculation,
    type = type,
    capacite = capacite,
    etat = etat,
    hangarId = hangarId
)

fun Avion.toEntity() = AvionEntity(
    id = id,
    immatriculation = immatriculation,
    type = type,
    capacite = capacite,
    etat = etat,
    hangarId = hangarId
)
