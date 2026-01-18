package fr.uga.miage.m1.domain.service

import fr.uga.miage.m1.domain.model.VolHistory
import fr.uga.miage.m1.domain.port.VolHistoryDataPort
import fr.uga.miage.m1.exceptions.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class VolHistoryService(
    private val volHistoryPort: VolHistoryDataPort
) {

    fun getHistoryForVol(volId: UUID): Flux<VolHistory> =
        volHistoryPort.findByVolId(volId)
            .sort(compareBy { it.changedAt })

    fun save(history: VolHistory): Mono<VolHistory> =
        volHistoryPort.save(history)
}
