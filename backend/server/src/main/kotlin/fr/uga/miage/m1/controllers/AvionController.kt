package fr.uga.miage.m1.controllers

import fr.uga.miage.m1.endpoints.AvionEndpoint
import fr.uga.miage.m1.requests.CreateAvionRequest
import fr.uga.miage.m1.requests.UpdateAvionRequest
import fr.uga.miage.m1.responses.AvionResponse
import fr.uga.miage.m1.mappers.toResponse
import fr.uga.miage.m1.services.AvionService
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class AvionController(private val service: AvionService) : AvionEndpoint {
    override fun list(): Flux<AvionResponse> = service.list().map { it.toResponse() }
    override fun get(id: UUID): Mono<AvionResponse> = service.get(id).map { it.toResponse() }
    override fun create(req: CreateAvionRequest): Mono<AvionResponse> = service.create(req).map { it.toResponse() }
    override fun update(id: UUID, req: UpdateAvionRequest): Mono<AvionResponse> = service.update(id, req).map { it.toResponse() }
    override fun delete(id: UUID): Mono<Void> = service.delete(id)
    override fun assignHangar(id: UUID, hangarId: UUID): Mono<AvionResponse> = service.assignHangar(id, hangarId).map { it.toResponse() }
    override fun unassignHangar(id: UUID): Mono<AvionResponse> = service.unassignHangar(id).map { it.toResponse() }
}
