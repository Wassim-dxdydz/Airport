package fr.uga.miage.m1.services

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.models.Vol
import fr.uga.miage.m1.repositories.VolRepository
import fr.uga.miage.m1.repositories.AvionRepository
import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.requests.UpdateVolRequest
import fr.uga.miage.m1.exceptions.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class VolService(
    private val repo: VolRepository,
    private val avions: AvionRepository,
    private val statusStrategy: VolStatusStrategy = DefaultVolStatusStrategy
) : BaseCrudService<Vol>(repo) {

    fun create(req: CreateVolRequest): Mono<Vol> =
        repo.save(Vol.create(
            numeroVol = req.numeroVol,
            origine = req.origine,
            destination = req.destination,
            heureDepart = req.heureDepart,
            heureArrivee = req.heureArrivee
        ))

    fun update(id: UUID, req: UpdateVolRequest): Mono<Vol> =
        get(id).flatMap { current ->
            val updated = current.copy(
                origine = req.origine ?: current.origine,
                destination = req.destination ?: current.destination,
                heureDepart = req.heureDepart ?: current.heureDepart,
                heureArrivee = req.heureArrivee ?: current.heureArrivee,
                etat = req.etat ?: current.etat,
                avionId = req.avionId ?: current.avionId
            )
            repo.save(updated)
        }

    fun assignAvion(id: UUID, avionId: UUID): Mono<Vol> =
        avions.existsById(avionId).flatMap { exists ->
            if (!exists) Mono.error(NotFoundException("Avion $avionId non trouvé"))
            else get(id).flatMap { repo.save(it.copy(avionId = avionId)) }
        }

    fun unassignAvion(id: UUID): Mono<Vol> =
        get(id).flatMap { repo.save(it.copy(avionId = null)) }

    fun updateEtat(id: UUID, etat: VolEtat): Mono<Vol> =
        get(id).flatMap { current ->
            if (!statusStrategy.canTransition(current.etat, etat))
                Mono.error(IllegalArgumentException("Transition de ${current.etat} vers $etat non autorisée"))
            else repo.save(current.copy(etat = etat))
        }

    fun listByEtat(etat: VolEtat): Flux<Vol> = repo.findByEtat(etat)
}
