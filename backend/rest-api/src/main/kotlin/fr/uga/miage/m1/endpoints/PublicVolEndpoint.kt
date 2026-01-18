package fr.uga.miage.m1.endpoints

import fr.uga.miage.m1.models.VolDto
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Flux

@RequestMapping("/api/public/vols")
interface PublicVolEndpoint {

    @GetMapping(value = ["/departures/{origin}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getDepartures(@PathVariable origin: String): Flux<fr.uga.miage.m1.models.VolDto>
}
