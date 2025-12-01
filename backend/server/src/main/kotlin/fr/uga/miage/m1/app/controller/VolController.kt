package fr.uga.miage.m1.app.controller

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.app.mapper.VolMapper
import fr.uga.miage.m1.domain.service.VolService
import fr.uga.miage.m1.endpoints.VolEndpoint
import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.requests.UpdateVolRequest
import fr.uga.miage.m1.responses.VolResponse
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class VolController(
    private val volService: VolService
) : VolEndpoint {

    override fun list(): Flux<VolResponse> =
        volService.list().map(VolMapper::toResponse)

    override fun get(id: UUID): Mono<VolResponse> =
        volService.get(id).map(VolMapper::toResponse)

    override fun create(req: CreateVolRequest): Mono<VolResponse> =
        volService.create(VolMapper.toDomain(req))
            .map(VolMapper::toResponse)

    override fun update(id: UUID, req: UpdateVolRequest): Mono<VolResponse> =
        volService.get(id)
            .flatMap { current ->
                val updated = VolMapper.toUpdatedDomain(current, req)
                volService.update(id, updated)
            }
            .map(VolMapper::toResponse)

    override fun delete(id: UUID): Mono<Void> =
        volService.delete(id)

    override fun assignAvion(id: UUID, avionId: UUID): Mono<VolResponse> =
        volService.assignAvion(id, avionId)
            .map(VolMapper::toResponse)

    override fun unassignAvion(id: UUID): Mono<VolResponse> =
        volService.unassignAvion(id)
            .map(VolMapper::toResponse)

    override fun updateEtat(id: UUID, etat: VolEtat): Mono<VolResponse> =
        volService.updateEtat(id, etat)
            .map(VolMapper::toResponse)

    override fun listByEtat(etat: VolEtat): Flux<VolResponse> =
        volService.listByEtat(etat)
            .map(VolMapper::toResponse)
}
