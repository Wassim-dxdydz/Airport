package fr.uga.miage.m1.persistence.adapter

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.port.VolDataPort
import fr.uga.miage.m1.persistence.entity.VolEntity
import fr.uga.miage.m1.persistence.repository.VolRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class VolAdapter(
    private val repo: VolRepository
) : VolDataPort {

    override fun findAll(): Flux<Vol> =
        repo.findAll().map { it.toDomain() }

    override fun findById(id: UUID): Mono<Vol> =
        repo.findById(id).map { it.toDomain() }

    override fun save(vol: Vol): Mono<Vol> =
        repo.save(vol.toEntity()).map { it.toDomain() }

    override fun deleteById(id: UUID): Mono<Unit> =
        repo.deleteById(id).thenReturn(Unit)

    override fun findByNumeroVol(numeroVol: String): Mono<Vol> =
        repo.findByNumeroVol(numeroVol).map { it.toDomain() }

    override fun deleteByNumeroVol(numeroVol: String): Mono<Unit> =
        repo.deleteByNumeroVol(numeroVol).thenReturn(Unit)

    override fun findByEtat(etat: VolEtat): Flux<Vol> =
        repo.findByEtat(etat).map { it.toDomain() }

    override fun findByAvionId(avionId: UUID): Flux<Vol> =
        repo.findByAvionId(avionId).map { it.toDomain() }

    override fun findByOrigine(origine: String): Flux<Vol> =
        repo.findByOrigine(origine).map { it.toDomain() }

    override fun findByDestination(destination: String): Flux<Vol> =
        repo.findByDestination(destination).map { it.toDomain() }

    override fun findByOrigineAndDestination(origine: String, destination: String): Flux<Vol> =
        repo.findByOrigineAndDestination(origine, destination).map { it.toDomain() }

    override fun findByPisteId(pisteId: UUID): Flux<Vol> =
        repo.findByPisteId(pisteId).map(VolEntity::toDomain)

    override fun existsByAvionIdAndEtatIn(avionId: UUID, etats: Set<VolEtat>): Mono<Boolean> =
        repo.existsByAvionIdAndEtatIn(avionId, etats)

    override fun existsById(id: UUID): Mono<Boolean> =
        repo.existsById(id)

}

/* Mapping */

fun VolEntity.toDomain() = Vol(
    id = id,
    numeroVol = numeroVol,
    origine = origine,
    destination = destination,
    heureDepart = heureDepart,
    heureArrivee = heureArrivee,
    etat = etat,
    avionId = avionId,
    pisteId = pisteId
)

fun Vol.toEntity() = VolEntity(
    id = id,
    numeroVol = numeroVol,
    origine = origine,
    destination = destination,
    heureDepart = heureDepart,
    heureArrivee = heureArrivee,
    etat = etat,
    avionId = avionId,
    pisteId = pisteId
)
