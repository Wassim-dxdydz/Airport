package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.PisteMapper
import fr.uga.miage.m1.domain.service.PisteService
import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteEtatRequest
import fr.uga.miage.m1.responses.PisteResponse
import fr.uga.miage.m1.endpoints.PisteEndpoint
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class PisteController(
    private val pisteService: PisteService
) : PisteEndpoint {

    override fun list(): Flux<PisteResponse> =
        pisteService.list()
            .map(PisteMapper::toResponse)

    override fun get(id: UUID): Mono<PisteResponse> =
        pisteService.get(id)
            .map(PisteMapper::toResponse)

    override fun create(req: CreatePisteRequest): Mono<PisteResponse> =
        pisteService.create(PisteMapper.toDomain(req))
            .map(PisteMapper::toResponse)

    override fun updateEtat(id: UUID, req: UpdatePisteEtatRequest): Mono<PisteResponse> =
        pisteService.updateEtat(id, req.etat)
            .map(PisteMapper::toResponse)

    override fun delete(id: UUID): Mono<Void> =
        pisteService.delete(id)

    override fun disponibles(): Flux<PisteResponse> =
        pisteService.disponibles()
            .map(PisteMapper::toResponse)
}
