package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.AvionMapper
import fr.uga.miage.m1.domain.service.AvionService
import fr.uga.miage.m1.requests.CreateAvionRequest
import fr.uga.miage.m1.requests.UpdateAvionRequest
import fr.uga.miage.m1.responses.AvionResponse
import fr.uga.miage.m1.endpoints.AvionEndpoint
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.UUID

@RestController
class AvionController(
    private val avionService: AvionService
) : AvionEndpoint {

    override fun list(): Flux<AvionResponse> =
        avionService.list().map(AvionMapper::toResponse)

    override fun get(id: UUID): Mono<AvionResponse> =
        avionService.get(id).map(AvionMapper::toResponse)

    override fun create(req: CreateAvionRequest): Mono<AvionResponse> =
        avionService.create(AvionMapper.toDomain(req)).map(AvionMapper::toResponse)

    override fun update(id: UUID, req: UpdateAvionRequest): Mono<AvionResponse> =
        avionService.get(id)
            .flatMap { avionService.update(id, AvionMapper.toUpdatedDomain(it, req)) }
            .map(AvionMapper::toResponse)

    override fun delete(id: UUID): Mono<Unit> =
        avionService.delete(id).thenReturn(Unit)

    override fun assignHangar(id: UUID, hangarId: UUID): Mono<AvionResponse> =
        avionService.assignHangar(id, hangarId).map(AvionMapper::toResponse)

    override fun unassignHangar(id: UUID): Mono<AvionResponse> =
        avionService.unassignHangar(id).map(AvionMapper::toResponse)
}

