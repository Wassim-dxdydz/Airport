package fr.uga.miage.m1

import fr.uga.miage.m1.models.VolDto
import reactor.core.publisher.Flux

interface ApiClient {
    fun getDeparturesFromATL(): Flux<VolDto>
}
