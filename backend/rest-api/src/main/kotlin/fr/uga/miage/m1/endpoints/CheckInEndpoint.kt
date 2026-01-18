package fr.uga.miage.m1.endpoints

import fr.uga.miage.m1.requests.CreateCheckInRequest
import fr.uga.miage.m1.requests.UpdateCheckInRequest
import fr.uga.miage.m1.responses.CheckInResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RequestMapping("/api/checkins")
interface CheckInEndpoint {

    @GetMapping
    fun list(): Flux<CheckInResponse>

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): Mono<CheckInResponse>

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: CreateCheckInRequest): Mono<CheckInResponse>

    @PatchMapping("/{id}")
    fun patch(@PathVariable id: UUID, @RequestBody req: UpdateCheckInRequest): Mono<CheckInResponse>

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID): Mono<Unit>

    @GetMapping("/vol/{volId}")
    fun listByVol(@PathVariable volId: UUID): Flux<CheckInResponse>

    @GetMapping("/passager/{passagerId}")
    fun listByPassager(@PathVariable passagerId: UUID): Flux<CheckInResponse>

    @GetMapping("/verify")
    fun verifyPassengerCheckIn(
        @RequestParam volId: UUID,
        @RequestParam passagerId: UUID
    ): Mono<Boolean>
}
