package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.CheckInMapper
import fr.uga.miage.m1.app.mapper.PassengerMapper
import fr.uga.miage.m1.domain.port.PassengerDataPort
import fr.uga.miage.m1.domain.service.CheckInService
import fr.uga.miage.m1.endpoints.CheckInEndpoint
import fr.uga.miage.m1.requests.CreateCheckInRequest
import fr.uga.miage.m1.requests.UpdateCheckInRequest
import fr.uga.miage.m1.responses.CheckInResponse
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class CheckInController(
    private val checkInService: CheckInService,
    private val passengerPort: PassengerDataPort
) : CheckInEndpoint {

    override fun list(): Flux<CheckInResponse> =
        checkInService.list()
            .flatMap { enrichCheckInResponse(it) }

    override fun get(id: UUID): Mono<CheckInResponse> =
        checkInService.get(id)
            .flatMap { enrichCheckInResponse(it) }

    override fun create(req: CreateCheckInRequest): Mono<CheckInResponse> =
        checkInService.create(CheckInMapper.toDomain(req))
            .flatMap { enrichCheckInResponse(it) }

    override fun patch(id: UUID, req: UpdateCheckInRequest): Mono<CheckInResponse> =
        checkInService.get(id)
            .flatMap { checkInService.update(id, CheckInMapper.toUpdatedDomain(it, req)) }
            .flatMap { enrichCheckInResponse(it) }

    override fun delete(id: UUID): Mono<Unit> =
        checkInService.delete(id)

    override fun listByVol(volId: UUID): Flux<CheckInResponse> =
        checkInService.listByVol(volId)
            .flatMap { enrichCheckInResponse(it) }

    override fun listByPassager(passagerId: UUID): Flux<CheckInResponse> =
        checkInService.listByPassager(passagerId)
            .flatMap { enrichCheckInResponse(it) }

    override fun verifyPassengerCheckIn(volId: UUID, passagerId: UUID): Mono<Boolean> =
        checkInService.verifyPassengerCheckIn(volId, passagerId)

    private fun enrichCheckInResponse(checkIn: fr.uga.miage.m1.domain.model.CheckIn): Mono<CheckInResponse> {
        return passengerPort.findById(checkIn.passagerId)
            .map { passenger ->
                CheckInMapper.toResponse(
                    checkIn = checkIn,
                    passagerInfo = PassengerMapper.toResponse(passenger)
                )
            }
            .switchIfEmpty(
                Mono.just(CheckInMapper.toResponse(checkIn, null))
            )
            .onErrorResume {
                Mono.just(CheckInMapper.toResponse(checkIn, null))
            }
    }
}
