package fr.uga.miage.m1.endpoints

import fr.uga.miage.m1.requests.CreateAvionRequest
import fr.uga.miage.m1.requests.UpdateAvionRequest
import fr.uga.miage.m1.responses.AvionResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RequestMapping("/api/avions")
interface AvionEndpoint {

    @GetMapping
    fun list(): Flux<AvionResponse>

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): Mono<AvionResponse>

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: CreateAvionRequest): Mono<AvionResponse>

    @PatchMapping("/{id}")
    fun patch(@PathVariable id: UUID, @RequestBody req: UpdateAvionRequest): Mono<AvionResponse>

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID): Mono<Unit>

    // Association Hangar ↔ Avion
    @PostMapping("/{id}/assign-hangar/{hangarId}")
    fun assignHangar(@PathVariable id: UUID, @PathVariable hangarId: UUID): Mono<AvionResponse>

    @PostMapping("/{id}/unassign-hangar")
    fun unassignHangar(@PathVariable id: UUID): Mono<AvionResponse>
}
