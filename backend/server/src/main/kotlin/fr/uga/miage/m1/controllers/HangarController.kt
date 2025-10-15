package fr.uga.miage.m1.controllers

import fr.uga.miage.m1.endpoints.HangarEndpoint
import fr.uga.miage.m1.requests.CreateHangarRequest
import fr.uga.miage.m1.requests.UpdateHangarRequest
import fr.uga.miage.m1.responses.AvionResponse
import fr.uga.miage.m1.responses.HangarResponse
import fr.uga.miage.m1.mappers.toResponse
import fr.uga.miage.m1.services.HangarService
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class HangarController(private val service: HangarService) : HangarEndpoint {
    override fun list(): Flux<HangarResponse> = service.list().map { it.toResponse() }
    override fun get(id: UUID): Mono<HangarResponse> = service.get(id).map { it.toResponse() }
    override fun create(req: CreateHangarRequest): Mono<HangarResponse> = service.create(req).map { it.toResponse() }
    override fun update(id: UUID, req: UpdateHangarRequest): Mono<HangarResponse> = service.update(id, req).map { it.toResponse() }
    override fun delete(id: UUID): Mono<Void> = service.delete(id)
    override fun listAvions(id: UUID): Flux<AvionResponse> =
        service.listAvions(id).map { it.toResponse() }
}
