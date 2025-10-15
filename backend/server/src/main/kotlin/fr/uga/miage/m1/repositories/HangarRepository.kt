package fr.uga.miage.m1.repositories

import fr.uga.miage.m1.models.Hangar
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID

interface HangarRepository : ReactiveCrudRepository<Hangar, UUID> {
    fun findByIdentifiant(identifiant: String): Mono<Hangar>
}
