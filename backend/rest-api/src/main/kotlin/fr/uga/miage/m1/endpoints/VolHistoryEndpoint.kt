package fr.uga.miage.m1.endpoints

import fr.uga.miage.m1.responses.VolHistoryResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.util.UUID

@RequestMapping("/api/vols/{volId}/history")
interface VolHistoryEndpoint {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getHistory(@PathVariable volId: UUID): Flux<VolHistoryResponse>
}
