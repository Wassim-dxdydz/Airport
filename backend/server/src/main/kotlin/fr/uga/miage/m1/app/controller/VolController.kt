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
import org.springframework.beans.factory.annotation.Value

@RestController
class VolController(
    private val volService: VolService,
    @Value("\${local.airport.code}") private val airportCode: String
) : VolEndpoint {

    override fun list(): Flux<VolResponse> =
        volService.list().map(VolMapper::toResponse)

    override fun get(id: UUID): Mono<VolResponse> =
        volService.get(id).map(VolMapper::toResponse)

    override fun create(req: CreateVolRequest): Mono<VolResponse> =
        volService.create(VolMapper.toDomain(req))
            .map(VolMapper::toResponse)

    override fun patch(id: UUID, req: UpdateVolRequest): Mono<VolResponse> =
        volService.get(id)
            .flatMap { current ->
                val updated = VolMapper.toPatchedDomain(current, req)
                volService.updateBasicFields(id, updated)
            }
            .map(VolMapper::toResponse)

    override fun delete(id: UUID): Mono<Unit> =
        volService.delete(id).thenReturn(Unit)

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

    override fun assignPiste(id: UUID, pisteId: UUID): Mono<VolResponse> =
        volService.assignPiste(id, pisteId)
            .map(VolMapper::toResponse)

    override fun releasePiste(id: UUID): Mono<VolResponse> =
        volService.releasePiste(id)
            .map(VolMapper::toResponse)

    override fun listDepartures(): Flux<VolResponse> =
        volService.listDeparturesFrom(airportCode)
            .map(VolMapper::toResponse)

    override fun listArrivals(): Flux<VolResponse> =
        volService.listArrivalsTo(airportCode)
            .map(VolMapper::toResponse)

    override fun traffic(): Flux<VolResponse> =
        volService.trafficFor(airportCode)
            .map(VolMapper::toResponse)

}
