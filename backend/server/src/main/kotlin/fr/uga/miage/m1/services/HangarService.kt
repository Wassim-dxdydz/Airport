package fr.uga.miage.m1.services

import fr.uga.miage.m1.requests.CreateHangarRequest
import fr.uga.miage.m1.requests.UpdateHangarRequest
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.models.Hangar
import fr.uga.miage.m1.repositories.AvionRepository
import fr.uga.miage.m1.repositories.HangarRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class HangarService(
    private val repo: HangarRepository,
    private val avions: AvionRepository
) {
    fun list(): Flux<Hangar> = repo.findAll()

    fun get(id: UUID): Mono<Hangar> =
        repo.findById(id).switchIfEmpty(Mono.error(NotFoundException("Hangar $id not found")))

    fun create(req: CreateHangarRequest): Mono<Hangar> =
        repo.save(Hangar(identifiant = req.identifiant, capacite = req.capacite, etat = req.etat))

    fun update(id: UUID, req: UpdateHangarRequest): Mono<Hangar> =
        get(id).flatMap { current ->
            repo.save(
                current.copy(
                    capacite = req.capacite ?: current.capacite,
                    etat = req.etat ?: current.etat
                )
            )
        }

    fun delete(id: UUID): Mono<Void> = repo.deleteById(id)

    fun listAvions(hangarId: UUID) = avions.findByHangarId(hangarId)
}
