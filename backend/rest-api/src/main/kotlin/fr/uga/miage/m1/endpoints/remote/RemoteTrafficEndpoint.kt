package fr.uga.miage.m1.endpoints.remote

import fr.uga.miage.m1.responses.VolResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import reactor.core.publisher.Flux

@RequestMapping("/api/remote")
interface RemoteTrafficEndpoint {

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.OK)
    fun syncIncomingFlights(): Flux<VolResponse>
}
