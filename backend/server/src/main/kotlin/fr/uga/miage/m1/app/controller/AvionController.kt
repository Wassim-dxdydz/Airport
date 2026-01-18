package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.AvionMapper
import fr.uga.miage.m1.domain.port.HangarDataPort
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
    private val avionService: AvionService,
    private val hangarPort: HangarDataPort
) : AvionEndpoint {

    override fun list(): Flux<AvionResponse> =
        avionService.list()
            .collectList()
            .flatMapMany { avions ->
                val hangarIds = avions.mapNotNull { it.hangarId }.toSet()

                if (hangarIds.isEmpty()) {
                    Flux.fromIterable(avions)
                        .map { AvionMapper.toResponse(it, "Non assigné") }
                } else {
                    hangarPort.findAllByIds(hangarIds)
                        .collectMap({ it.id!! }, { it.identifiant })
                        .flatMapMany { hangarMap ->
                            Flux.fromIterable(avions)
                                .map { avion ->
                                    val identifiant =
                                        avion.hangarId?.let { hangarMap[it] } ?: "Non assigné"
                                    AvionMapper.toResponse(avion, identifiant)
                                }
                        }
                }
            }

    override fun get(id: UUID): Mono<AvionResponse> =
        avionService.get(id)
            .flatMap { avion ->
                val hangarId = avion.hangarId
                if (hangarId == null) {
                    Mono.just(AvionMapper.toResponse(avion, "Non assigné"))
                } else {
                    hangarPort.findById(hangarId)
                        .map { hangar -> AvionMapper.toResponse(avion, hangar.identifiant) }
                        .switchIfEmpty(Mono.just(AvionMapper.toResponse(avion, "Non assigné")))
                }
            }


    override fun create(req: CreateAvionRequest): Mono<AvionResponse> =
        avionService.create(AvionMapper.toDomain(req)).map(AvionMapper::toResponse)

    override fun patch(id: UUID, req: UpdateAvionRequest): Mono<AvionResponse> =
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
