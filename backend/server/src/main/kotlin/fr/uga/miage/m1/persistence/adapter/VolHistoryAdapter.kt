package fr.uga.miage.m1.persistence.adapter

import fr.uga.miage.m1.domain.model.VolHistory
import fr.uga.miage.m1.domain.port.VolHistoryDataPort
import fr.uga.miage.m1.app.mapper.VolHistoryMapper
import fr.uga.miage.m1.persistence.repository.VolHistoryRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class VolHistoryAdapter(
    private val repository: VolHistoryRepository
) : VolHistoryDataPort {

    override fun save(history: VolHistory): Mono<VolHistory> =
        repository.save(VolHistoryMapper.toEntity(history))
            .map(VolHistoryMapper::toDomain)

    override fun findByVolId(volId: UUID): Flux<VolHistory> =
        repository.findByVolId(volId)
            .map(VolHistoryMapper::toDomain)
}

