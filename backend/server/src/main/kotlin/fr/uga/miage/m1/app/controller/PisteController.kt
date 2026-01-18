package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.PisteMapper
import fr.uga.miage.m1.app.mapper.VolMapper
import fr.uga.miage.m1.responses.VolResponse
import fr.uga.miage.m1.domain.service.PisteService
import fr.uga.miage.m1.domain.service.VolService
import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteRequest
import fr.uga.miage.m1.responses.PisteResponse
import fr.uga.miage.m1.endpoints.PisteEndpoint
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class PisteController(
    private val pisteService: PisteService,
    private val volService: VolService
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

    override fun patch(id: UUID, req: UpdatePisteRequest): Mono<PisteResponse> =
        pisteService.get(id)
            .flatMap { current ->
                val updated = PisteMapper.toUpdatedDomain(current, req)
                pisteService.update(id, updated)
            }
            .map(PisteMapper::toResponse)

    override fun delete(id: UUID): Mono<Unit> =
        pisteService.delete(id).thenReturn(Unit)

    override fun disponibles(): Flux<PisteResponse> =
        pisteService.disponibles()
            .map(PisteMapper::toResponse)

    override fun planning(id: UUID): Flux<VolResponse> =
        volService.listByPiste(id)
            .map(VolMapper::toResponse)
}
