package fr.uga.miage.m1.domain.port

import fr.uga.miage.m1.domain.model.VolHistory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface VolHistoryDataPort {
    fun save(history: VolHistory): Mono<VolHistory>
    fun findByVolId(volId: UUID): Flux<VolHistory>
}
