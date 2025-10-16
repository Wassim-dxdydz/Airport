package fr.uga.miage.m1.controllers

import fr.uga.miage.m1.endpoints.VolEndpoint
import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.requests.UpdateVolRequest
import fr.uga.miage.m1.responses.VolResponse
import fr.uga.miage.m1.mappers.toResponse
import fr.uga.miage.m1.services.VolService
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class VolController(private val service: VolService) : VolEndpoint {
    override fun list(): Flux<VolResponse> = service.list().map { it.toResponse() }
    override fun get(id: UUID): Mono<VolResponse> = service.get(id).map { it.toResponse() }
    override fun create(req: CreateVolRequest): Mono<VolResponse> = service.create(req).map { it.toResponse() }
    override fun update(id: UUID, req: UpdateVolRequest): Mono<VolResponse> = service.update(id, req).map { it.toResponse() }
    override fun delete(id: UUID): Mono<Void> = service.delete(id)
    override fun assignAvion(id: UUID, avionId: UUID): Mono<VolResponse> =
        service.assignAvion(id, avionId).map { it.toResponse() }
    override fun unassignAvion(id: UUID): Mono<VolResponse> =
        service.unassignAvion(id).map { it.toResponse() }
    override fun updateEtat(id: UUID, etat: VolEtat): Mono<VolResponse> =
        service.updateEtat(id, etat).map { it.toResponse() }
    override fun listByEtat(etat: VolEtat): Flux<VolResponse> =
        service.listByEtat(etat).map { it.toResponse() }
}
