package fr.uga.miage.m1.persistence.adapter

import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.domain.port.HangarDataPort
import fr.uga.miage.m1.persistence.entity.HangarEntity
import fr.uga.miage.m1.persistence.repository.HangarRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class HangarAdapter(
    private val repo: HangarRepository
) : HangarDataPort {

    override fun findAll(): Flux<Hangar> =
        repo.findAll().map { it.toDomain() }

    override fun findById(id: UUID): Mono<Hangar> =
        repo.findById(id).map { it.toDomain() }

    override fun findByIdentifiant(identifiant: String): Mono<Hangar> =
        repo.findByIdentifiant(identifiant).map { it.toDomain() }

    override fun save(hangar: Hangar): Mono<Hangar> =
        repo.save(hangar.toEntity()).map { it.toDomain() }

    override fun deleteById(id: UUID): Mono<Void> =
        repo.deleteById(id)

    override fun deleteByIdentifiant(identifiant: String): Mono<Void> =
        repo.deleteByIdentifiant(identifiant)

    override fun existsById(id: UUID): Mono<Boolean> =
        repo.existsById(id)
}

fun HangarEntity.toDomain() = Hangar(id, identifiant, capacite, etat)

fun Hangar.toEntity() = HangarEntity(id, identifiant, capacite, etat)
