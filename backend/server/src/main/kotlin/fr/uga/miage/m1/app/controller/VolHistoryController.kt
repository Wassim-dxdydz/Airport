package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.VolHistoryMapper
import fr.uga.miage.m1.domain.port.VolHistoryDataPort
import fr.uga.miage.m1.endpoints.VolHistoryEndpoint
import fr.uga.miage.m1.responses.VolHistoryResponse
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.UUID

@RestController
class VolHistoryController(
    private val volHistoryPort: VolHistoryDataPort
) : VolHistoryEndpoint {

    override fun getHistory(volId: UUID): Flux<VolHistoryResponse> =
        volHistoryPort.findByVolId(volId)
            .map(VolHistoryMapper::toResponse)
}
