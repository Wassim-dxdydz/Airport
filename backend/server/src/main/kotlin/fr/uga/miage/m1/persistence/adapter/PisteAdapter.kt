package fr.uga.miage.m1.persistence.adapter

import fr.uga.miage.m1.domain.model.Piste
import fr.uga.miage.m1.domain.port.PisteDataPort
import fr.uga.miage.m1.persistence.entity.PisteEntity
import fr.uga.miage.m1.persistence.repository.PisteRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class PisteAdapter(
    private val repo: PisteRepository
) : PisteDataPort {

    override fun findAll(): Flux<Piste> =
        repo.findAll().map { it.toDomain() }

    override fun findById(id: UUID): Mono<Piste> =
        repo.findById(id).map { it.toDomain() }

    override fun save(piste: Piste): Mono<Piste> =
        repo.save(piste.toEntity()).map { it.toDomain() }

    override fun deleteById(id: UUID): Mono<Void> =
        repo.deleteById(id)

    override fun findByEtat(etat: backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat): Flux<Piste> =
        repo.findByEtat(etat).map { it.toDomain() }

    override fun existsById(id: UUID): Mono<Boolean> =
        repo.existsById(id)
}

fun PisteEntity.toDomain() = Piste(
    id = id,
    identifiant = identifiant,
    longueurM = longueurM,
    etat = etat
)

fun Piste.toEntity() = PisteEntity(
    id = id,
    identifiant = identifiant,
    longueurM = longueurM,
    etat = etat
)
