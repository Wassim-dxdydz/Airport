package fr.uga.miage.m1.endpoints

import fr.uga.miage.m1.requests.CreateHangarRequest
import fr.uga.miage.m1.requests.UpdateHangarRequest
import fr.uga.miage.m1.responses.AvionResponse
import fr.uga.miage.m1.responses.HangarResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RequestMapping("/api/hangars")
interface HangarEndpoint {

    @GetMapping
    fun list(): Flux<HangarResponse>

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): Mono<HangarResponse>

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: CreateHangarRequest): Mono<HangarResponse>

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody req: UpdateHangarRequest): Mono<HangarResponse>

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID): Mono<Unit>

    // Lister les avions présents dans un hangar
    @GetMapping("/{id}/avions")
    fun listAvions(@PathVariable id: UUID): Flux<AvionResponse>
}
