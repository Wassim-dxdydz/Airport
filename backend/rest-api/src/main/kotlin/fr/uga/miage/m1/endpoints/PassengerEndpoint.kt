package fr.uga.miage.m1.endpoints

import fr.uga.miage.m1.requests.CreatePassengerRequest
import fr.uga.miage.m1.requests.UpdatePassengerRequest
import fr.uga.miage.m1.responses.PassengerResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RequestMapping("/api/passagers")
interface PassengerEndpoint {

    @GetMapping
    fun list(): Flux<PassengerResponse>

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): Mono<PassengerResponse>

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: CreatePassengerRequest): Mono<PassengerResponse>

    @PatchMapping("/{id}")
    fun patch(@PathVariable id: UUID, @RequestBody req: UpdatePassengerRequest): Mono<PassengerResponse>

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID): Mono<Unit>

    @GetMapping("/not-checked-in/{volId}")
    fun listNotCheckedInForVol(@PathVariable volId: UUID): Flux<PassengerResponse>
}
