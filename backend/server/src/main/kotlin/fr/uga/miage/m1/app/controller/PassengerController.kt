package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.PassengerMapper
import fr.uga.miage.m1.domain.service.PassengerService
import fr.uga.miage.m1.endpoints.PassengerEndpoint
import fr.uga.miage.m1.requests.CreatePassengerRequest
import fr.uga.miage.m1.requests.UpdatePassengerRequest
import fr.uga.miage.m1.responses.PassengerResponse
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class PassengerController(
    private val passengerService: PassengerService
) : PassengerEndpoint {

    override fun list(): Flux<PassengerResponse> =
        passengerService.list().map(PassengerMapper::toResponse)

    override fun get(id: UUID): Mono<PassengerResponse> =
        passengerService.get(id).map(PassengerMapper::toResponse)

    override fun create(req: CreatePassengerRequest): Mono<PassengerResponse> =
        passengerService.create(PassengerMapper.toDomain(req))
            .map(PassengerMapper::toResponse)

    override fun patch(id: UUID, req: UpdatePassengerRequest): Mono<PassengerResponse> =
        passengerService.get(id)
            .flatMap { passengerService.update(id, PassengerMapper.toUpdatedDomain(it, req)) }
            .map(PassengerMapper::toResponse)

    override fun delete(id: UUID): Mono<Unit> =
        passengerService.delete(id)

    override fun listNotCheckedInForVol(volId: UUID): Flux<PassengerResponse> =
        passengerService.listNotCheckedInForVol(volId)
            .map(PassengerMapper::toResponse)
}
