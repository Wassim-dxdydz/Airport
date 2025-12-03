package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.VolMapper
import fr.uga.miage.m1.domain.service.SharedVolSyncService
import fr.uga.miage.m1.endpoints.remote.RemoteTrafficEndpoint
import fr.uga.miage.m1.responses.VolResponse
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class RemoteTrafficController(
    private val syncService: SharedVolSyncService
) : RemoteTrafficEndpoint {

    override fun syncIncomingFlights(): Flux<VolResponse> =
        syncService.syncIncomingFlights()
            .map(VolMapper::toResponse)
}
