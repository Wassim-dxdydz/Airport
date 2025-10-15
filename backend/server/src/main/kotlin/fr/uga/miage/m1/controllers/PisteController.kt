package fr.uga.miage.m1.controllers

import fr.uga.miage.m1.endpoints.PisteEndpoint
import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteEtatRequest
import fr.uga.miage.m1.responses.PisteResponse
import fr.uga.miage.m1.mappers.toResponse
import fr.uga.miage.m1.services.PisteService
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class PisteController(private val service: PisteService) : PisteEndpoint {
    override fun list(): Flux<PisteResponse> = service.list().map { it.toResponse() }
    override fun get(id: UUID): Mono<PisteResponse> = service.get(id).map { it.toResponse() }
    override fun create(req: CreatePisteRequest): Mono<PisteResponse> = service.create(req).map { it.toResponse() }
    override fun updateEtat(id: UUID, req: UpdatePisteEtatRequest): Mono<PisteResponse> = service.updateEtat(id, req).map { it.toResponse() }
    override fun delete(id: UUID): Mono<Void> = service.delete(id)
    override fun disponibles(): Flux<PisteResponse> = service.disponibles().map { it.toResponse() }
}
