package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.HangarMapper
import fr.uga.miage.m1.domain.service.HangarService
import fr.uga.miage.m1.requests.CreateHangarRequest
import fr.uga.miage.m1.requests.UpdateHangarRequest
import fr.uga.miage.m1.responses.HangarResponse
import fr.uga.miage.m1.responses.AvionResponse
import fr.uga.miage.m1.app.mapper.AvionMapper
import org.springframework.web.bind.annotation.RestController
import fr.uga.miage.m1.endpoints.HangarEndpoint
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class HangarController(
    private val hangarService: HangarService
) : HangarEndpoint {

    override fun list(): Flux<HangarResponse> =
        hangarService.list()
            .map(HangarMapper::toResponse)

    override fun get(id: UUID): Mono<HangarResponse> =
        hangarService.get(id)
            .map(HangarMapper::toResponse)

    override fun create(req: CreateHangarRequest): Mono<HangarResponse> =
        hangarService.create(HangarMapper.toDomain(req))
            .map(HangarMapper::toResponse)

    override fun patch(id: UUID, req: UpdateHangarRequest): Mono<HangarResponse> =
        hangarService.get(id)
            .flatMap { current ->
                val updated = HangarMapper.toUpdatedDomain(current, req)
                hangarService.update(id, updated)
            }
            .map(HangarMapper::toResponse)

    override fun delete(id: UUID): Mono<Unit> =
        hangarService.delete(id).thenReturn(Unit)

    override fun listAvions(id: UUID): Flux<AvionResponse> =
        hangarService.listAvions(id)
            .map(AvionMapper::toResponse)
}
