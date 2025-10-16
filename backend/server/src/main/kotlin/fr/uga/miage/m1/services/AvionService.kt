package fr.uga.miage.m1.services

import fr.uga.miage.m1.requests.CreateAvionRequest
import fr.uga.miage.m1.requests.UpdateAvionRequest
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.models.Avion
import fr.uga.miage.m1.repositories.AvionRepository
import fr.uga.miage.m1.repositories.HangarRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class AvionService(
    private val repo: AvionRepository,
    private val hangars: HangarRepository
) : BaseCrudService<Avion>(repo) {

    fun create(req: CreateAvionRequest): Mono<Avion> =
        (req.hangarId?.let { hangars.existsById(it) } ?: Mono.just(true))
            .flatMap { exists ->
                if (!exists) Mono.error(NotFoundException("Hangar ${req.hangarId} not found"))
                else repo.save(
                    Avion.create(
                        immatriculation = req.immatriculation,
                        type = req.type,
                        capacite = req.capacite,
                        etat = req.etat ?: AvionEtat.EN_SERVICE,
                        hangarId = req.hangarId
                    )
                )
            }

    fun update(id: UUID, req: UpdateAvionRequest): Mono<Avion> =
        get(id).flatMap { current ->
            val targetHangar = req.hangarId
            val checkHangar = if (targetHangar != null) hangars.existsById(targetHangar) else Mono.just(true)
            checkHangar.flatMap { ok ->
                if (!ok) Mono.error(NotFoundException("Hangar $targetHangar not found"))
                else repo.save(
                    current.copy(
                        type = req.type ?: current.type,
                        capacite = req.capacite ?: current.capacite,
                        etat = req.etat ?: current.etat,
                        hangarId = req.hangarId ?: current.hangarId
                    )
                )
            }
        }

    fun assignHangar(id: UUID, hangarId: UUID): Mono<Avion> =
        hangars.existsById(hangarId).flatMap { exists ->
            if (!exists) Mono.error(NotFoundException("Hangar $hangarId not found"))
            else get(id).flatMap { repo.save(it.copy(hangarId = hangarId)) }
        }

    fun unassignHangar(id: UUID): Mono<Avion> =
        get(id).flatMap { repo.save(it.copy(hangarId = null)) }
}
