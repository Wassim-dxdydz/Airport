package fr.uga.miage.m1.endpoints

import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.requests.UpdateVolRequest
import fr.uga.miage.m1.responses.VolResponse
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RequestMapping("/api/vols")
interface VolEndpoint {

    @GetMapping
    fun list(): Flux<VolResponse>

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): Mono<VolResponse>

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: CreateVolRequest): Mono<VolResponse>

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody req: UpdateVolRequest): Mono<VolResponse>

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID): Mono<Unit>

    // Association Avion ↔ Vol
    @PostMapping("/{id}/assign-avion/{avionId}")
    fun assignAvion(@PathVariable id: UUID, @PathVariable avionId: UUID): Mono<VolResponse>

    @PostMapping("/{id}/unassign-avion")
    fun unassignAvion(@PathVariable id: UUID): Mono<VolResponse>

    // Etat management
    @PatchMapping("/{id}/etat")
    fun updateEtat(@PathVariable id: UUID, @RequestBody etat: VolEtat): Mono<VolResponse>

    @GetMapping("/etat/{etat}")
    fun listByEtat(@PathVariable etat: VolEtat): Flux<VolResponse>

    @PostMapping("/{id}/assign-piste/{pisteId}")
    fun assignPiste(@PathVariable id: UUID, @PathVariable pisteId: UUID): Mono<VolResponse>

    @PostMapping("/{id}/release-piste")
    fun releasePiste(@PathVariable id: UUID): Mono<VolResponse>

    @GetMapping("/departures")
    fun listDepartures(): Flux<VolResponse>

    @GetMapping("/arrivals")
    fun listArrivals(): Flux<VolResponse>

    @GetMapping("/traffic")
    fun traffic(): Flux<VolResponse>

}
