package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.VolDataPort
import fr.uga.miage.m1.exceptions.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class VolService(
    private val volPort: VolDataPort,
    private val avionPort: AvionDataPort,
    private val statusStrategy: VolStatusStrategy = DefaultVolStatusStrategy
) {

    fun list(): Flux<Vol> =
        volPort.findAll()

    fun get(id: UUID): Mono<Vol> =
        volPort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Vol $id not found")))

    fun create(vol: Vol): Mono<Vol> =
        volPort.save(
            vol.copy(
                etat = VolEtat.PREVU // enforce initial state
            )
        )

    fun update(id: UUID, updated: Vol): Mono<Vol> =
        get(id).flatMap { current ->
            val merged = current.copy(
                origine = updated.origine,
                destination = updated.destination,
                heureDepart = updated.heureDepart,
                heureArrivee = updated.heureArrivee,
                etat = updated.etat,
                avionId = updated.avionId
            )
            volPort.save(merged)
        }

    fun delete(id: UUID): Mono<Void> =
        volPort.deleteById(id)

    fun assignAvion(id: UUID, avionId: UUID): Mono<Vol> =
        avionPort.findById(avionId)
            .switchIfEmpty(Mono.error(NotFoundException("Avion $avionId non trouvé")))
            .flatMap {
                get(id)
            }
            .flatMap { vol ->
                volPort.save(vol.copy(avionId = avionId))
            }

    fun unassignAvion(id: UUID): Mono<Vol> =
        get(id)
            .flatMap { volPort.save(it.copy(avionId = null)) }

    fun updateEtat(id: UUID, etat: VolEtat): Mono<Vol> =
        get(id).flatMap { current ->
            if (!statusStrategy.canTransition(current.etat, etat)) {
                Mono.error(IllegalArgumentException("Transition de ${current.etat} vers $etat non autorisée"))
            } else {
                volPort.save(current.copy(etat = etat))
            }
        }

    fun listByEtat(etat: VolEtat): Flux<Vol> =
        volPort.findByEtat(etat)
}
