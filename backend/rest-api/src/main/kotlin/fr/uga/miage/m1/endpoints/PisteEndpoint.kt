package fr.uga.miage.m1.endpoints

import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteEtatRequest
import fr.uga.miage.m1.responses.PisteResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RequestMapping("/api/pistes")
interface PisteEndpoint {

    @GetMapping
    fun list(): Flux<PisteResponse>

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): Mono<PisteResponse>

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: CreatePisteRequest): Mono<PisteResponse>

    @PatchMapping("/{id}/etat")
    fun updateEtat(@PathVariable id: UUID, @RequestBody req: UpdatePisteEtatRequest): Mono<PisteResponse>

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID): Mono<Void>

    @GetMapping("/disponibles")
    fun disponibles(): Flux<PisteResponse> // etat = LIBRE

}
