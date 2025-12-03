package fr.uga.miage.m1.endpoints.remote

import fr.uga.miage.m1.requests.remote.SharedVolRequest
import fr.uga.miage.m1.responses.remote.SharedVolResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@RequestMapping("/api/shared/vols")
interface SharedVolEndpoint {

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    fun importVol(@RequestBody req: SharedVolRequest): Mono<Unit>

    @GetMapping("/export")
    fun exportVols(): Flux<SharedVolResponse>
}
