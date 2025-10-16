package fr.uga.miage.m1.services

import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteEtatRequest
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.models.Piste
import fr.uga.miage.m1.repositories.PisteRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class PisteService(private val repo: PisteRepository) : BaseCrudService<Piste>(repo) {

    fun create(req: CreatePisteRequest): Mono<Piste> =
        repo.save(Piste.create(
            identifiant = req.identifiant,
            longueurM = req.longueurM,
            etat = req.etat ?: PisteEtat.LIBRE
        ))

    fun updateEtat(id: UUID, req: UpdatePisteEtatRequest): Mono<Piste> =
        get(id).switchIfEmpty(Mono.error(NotFoundException("Piste $id not found")))
            .flatMap { existing ->
                repo.save(existing.copy(etat = req.etat))
            }

    fun disponibles(): Flux<Piste> = repo.findByEtat(PisteEtat.LIBRE)
}
