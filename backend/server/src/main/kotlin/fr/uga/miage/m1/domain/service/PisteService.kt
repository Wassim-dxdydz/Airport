package fr.uga.miage.m1.domain.service

import fr.uga.miage.m1.domain.model.Piste
import fr.uga.miage.m1.domain.port.PisteDataPort
import fr.uga.miage.m1.domain.state.PisteStateFactory
import fr.uga.miage.m1.exceptions.NotFoundException
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.domain.validation.PisteValidator
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.UUID

@Service
class PisteService(
    private val pistePort: PisteDataPort
) {

    fun list(): Flux<Piste> =
        pistePort.findAll()

    fun get(id: UUID): Mono<Piste> =
        pistePort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Piste $id non trouvée")))

    fun create(piste: Piste): Mono<Piste> {
        if (piste.etat != PisteEtat.LIBRE && piste.etat != PisteEtat.MAINTENANCE) {
            return Mono.error(
                IllegalStateException("Une piste ne peut être créée qu'avec l'état LIBRE ou MAINTENANCE")
            )
        }

        PisteValidator.validate(piste)
        return pistePort.save(piste)
    }

    fun update(id: UUID, updated: Piste): Mono<Piste> =
        get(id).flatMap { current ->
            val stateChangeRequested = updated.etat != current.etat

            val merged = current.copy(
                identifiant = updated.identifiant,
                longueurM = updated.longueurM,
                etat = current.etat
            )

            PisteValidator.validate(merged)

            when {
                stateChangeRequested -> {
                    val currentState = PisteStateFactory.fromEtat(current.etat)
                    val newState = currentState.transitionTo(updated.etat)
                    pistePort.save(merged.copy(etat = newState.getEtat()))
                }
                else -> pistePort.save(merged)
            }
        }

    fun updateEtat(id: UUID, newEtat: PisteEtat): Mono<Piste> =
        get(id).flatMap { current ->
            if (current.etat == newEtat) {
                return@flatMap Mono.just(current)
            }

            val currentState = PisteStateFactory.fromEtat(current.etat)
            val newState = currentState.transitionTo(newEtat)

            val updated = current.copy(etat = newState.getEtat())
            PisteValidator.validate(updated)
            pistePort.save(updated)
        }

    fun delete(id: UUID): Mono<Unit> =
        get(id).flatMap { piste ->
            if (piste.etat == PisteEtat.OCCUPEE) {
                Mono.error<Unit>(
                    IllegalStateException("Impossible de supprimer une piste encore occupée")
                )
            } else {
                pistePort.deleteById(id).thenReturn(Unit)
            }
        }

    fun disponibles(): Flux<Piste> =
        pistePort.findByEtat(PisteEtat.LIBRE)
}
