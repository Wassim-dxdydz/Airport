package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.VolHistoryMapper
import fr.uga.miage.m1.domain.service.VolHistoryService
import fr.uga.miage.m1.endpoints.VolHistoryEndpoint
import fr.uga.miage.m1.responses.VolHistoryResponse
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.UUID

@RestController
class VolHistoryController(
    private val volHistoryService: VolHistoryService
) : VolHistoryEndpoint {

    override fun getHistory(volId: UUID): Flux<VolHistoryResponse> =
        volHistoryService.getHistoryForVol(volId)
            .map(VolHistoryMapper::toResponse)
}
