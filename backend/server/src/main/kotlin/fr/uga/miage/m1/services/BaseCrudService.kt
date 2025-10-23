package fr.uga.miage.m1.services

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID
import fr.uga.miage.m1.exceptions.NotFoundException

abstract class BaseCrudService<T>(
    private val repo: ReactiveCrudRepository<T, UUID>,
    private val entityName: String
) {
    fun list(): Flux<T> = repo.findAll()

    fun get(id: UUID): Mono<T> =
        repo.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("$entityName $id not found")))

    fun delete(id: UUID): Mono<Void> = repo.deleteById(id)
}
