package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.SharedVolInboundMapper
import fr.uga.miage.m1.app.mapper.SharedVolOutboundMapper
import fr.uga.miage.m1.domain.service.SharedVolSyncService
import fr.uga.miage.m1.endpoints.remote.SharedVolEndpoint
import fr.uga.miage.m1.requests.remote.SharedVolRequest
import fr.uga.miage.m1.responses.remote.SharedVolResponse
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class RemoteSharedVolController(
    private val syncService: SharedVolSyncService
) : SharedVolEndpoint {

    override fun importVol(req: SharedVolRequest): Mono<Unit> {
        val (avion, vol) = SharedVolInboundMapper.toDomain(req)
        return syncService.import(avion, vol).then(Mono.just(Unit))
    }

    override fun exportVols(): Flux<SharedVolResponse> =
        syncService.exportForPartner()
            .map { (vol, avion) -> SharedVolOutboundMapper.toResponse(vol, avion) }
}
