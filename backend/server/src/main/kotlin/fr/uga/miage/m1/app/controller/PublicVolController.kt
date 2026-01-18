package fr.uga.miage.m1.application.controller

import fr.uga.miage.m1.endpoints.PublicVolEndpoint
import fr.uga.miage.m1.models.VolDto
import fr.uga.miage.m1.domain.service.VolService
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class PublicVolController(
    private val volService: VolService
) : PublicVolEndpoint {

    override fun getDepartures(origin: String): Flux<VolDto> {
        return volService.findDeparturesFromOrigin(origin)
    }
}
