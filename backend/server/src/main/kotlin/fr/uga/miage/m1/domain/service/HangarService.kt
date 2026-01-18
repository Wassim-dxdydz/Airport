package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.domain.port.HangarDataPort
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.state.HangarStateFactory
import fr.uga.miage.m1.domain.validation.HangarValidator
import fr.uga.miage.m1.exceptions.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class HangarService(
    private val hangarPort: HangarDataPort,
    private val avionPort: AvionDataPort
) {

    fun list(): Flux<Hangar> =
        hangarPort.findAll()

    fun get(id: UUID): Mono<Hangar> =
        hangarPort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Hangar $id non trouvé")))

    fun create(hangar: Hangar): Mono<Hangar> {
        if (hangar.etat != HangarEtat.DISPONIBLE && hangar.etat != HangarEtat.MAINTENANCE) {
            return Mono.error(
                IllegalStateException("Un hangar ne peut être créé qu'avec l'état DISPONIBLE ou MAINTENANCE")
            )
        }
        HangarValidator.validate(hangar)
        return hangarPort.save(hangar)
    }

    fun update(id: UUID, updated: Hangar): Mono<Hangar> =
        get(id).flatMap { current ->
            val capacityChanged = updated.capacite != current.capacite
            val stateChangeRequested = updated.etat != current.etat

            val merged = current.copy(
                identifiant = updated.identifiant,
                capacite = updated.capacite,
                etat = current.etat
            )

            HangarValidator.validate(merged)

            if (capacityChanged && updated.capacite < current.capacite) {
                avionPort.countByHangarId(id).flatMap { count ->
                    if (updated.capacite < count) {
                        Mono.error(IllegalStateException(
                            "Impossible de réduire la capacité à ${updated.capacite} : le hangar contient actuellement $count avion(s)"
                        ))
                    } else {
                        proceedWithUpdate(current, merged, updated, stateChangeRequested, capacityChanged, id)
                    }
                }
            } else {
                proceedWithUpdate(current, merged, updated, stateChangeRequested, capacityChanged, id)
            }
        }

    private fun proceedWithUpdate(
        current: Hangar,
        merged: Hangar,
        updated: Hangar,
        stateChangeRequested: Boolean,
        capacityChanged: Boolean,
        id: UUID
    ): Mono<Hangar> {
        return when {
            stateChangeRequested && updated.etat == HangarEtat.MAINTENANCE -> {
                avionPort.countByHangarId(id).flatMap { count ->
                    if (count > 0) {
                        Mono.error(IllegalStateException(
                            "Impossible de mettre le hangar en maintenance : il contient $count avion(s)"
                        ))
                    } else {
                        val newState = HangarStateFactory.fromEtat(current.etat)
                            .transitionTo(HangarEtat.MAINTENANCE, count.toInt(), merged.capacite)
                        hangarPort.save(merged.copy(etat = newState.getEtat()))
                    }
                }
            }

            stateChangeRequested && current.etat == HangarEtat.MAINTENANCE -> {
                hangarPort.save(merged).flatMap { saved ->
                    updateStateBasedOnOccupancy(saved.id!!)
                }
            }

            capacityChanged -> {
                hangarPort.save(merged).flatMap { saved ->
                    updateStateBasedOnOccupancy(saved.id!!)
                }
            }

            stateChangeRequested &&
                    (updated.etat == HangarEtat.DISPONIBLE || updated.etat == HangarEtat.PLEIN) -> {
                Mono.error(IllegalStateException(
                    "Impossible de modifier manuellement l'état en ${updated.etat}. " +
                            "Les états DISPONIBLE/PLEIN sont gérés automatiquement selon l'occupation."
                ))
            }

            else -> hangarPort.save(merged)
        }
    }

    fun delete(id: UUID): Mono<Unit> =
        get(id).flatMap {
            avionPort.findAll()
                .filter { avion -> avion.hangarId == id }
                .hasElements()
                .flatMap { hasAvions ->
                    if (hasAvions)
                        Mono.error<Unit>(IllegalStateException(
                            "Impossible de supprimer le hangar : il contient encore des avions"
                        ))
                    else
                        hangarPort.deleteById(id).thenReturn(Unit)
                }
        }

    fun listAvions(id: UUID): Flux<fr.uga.miage.m1.domain.model.Avion> =
        avionPort.findAll()
            .filter { it.hangarId == id }

    fun ensureCanAcceptAvion(hangarId: UUID): Mono<Hangar> =
        get(hangarId).flatMap { hangar ->
            when (hangar.etat) {
                HangarEtat.MAINTENANCE ->
                    Mono.error(IllegalStateException(
                        "Impossible d'assigner un avion : le hangar est en maintenance"
                    ))
                HangarEtat.PLEIN ->
                    Mono.error(IllegalStateException(
                        "Impossible d'assigner un avion : le hangar est plein"
                    ))
                HangarEtat.DISPONIBLE ->
                    avionPort.countByHangarId(hangarId).flatMap { count ->
                        if (count >= hangar.capacite) {
                            Mono.error(IllegalStateException(
                                "Impossible d'assigner un avion : le hangar est plein"
                            ))
                        } else {
                            Mono.just(hangar)
                        }
                    }
            }
        }

    fun updateStateBasedOnOccupancy(hangarId: UUID): Mono<Hangar> =
        get(hangarId).flatMap { hangar ->
            avionPort.countByHangarId(hangarId).flatMap { count ->
                val targetEtat = when {
                    hangar.etat == HangarEtat.MAINTENANCE -> HangarEtat.MAINTENANCE
                    count >= hangar.capacite -> HangarEtat.PLEIN
                    else -> HangarEtat.DISPONIBLE
                }

                if (targetEtat != hangar.etat) {
                    val currentState = HangarStateFactory.fromEtat(hangar.etat)
                    val newState = currentState.transitionTo(
                        targetEtat,
                        count.toInt(),
                        hangar.capacite
                    )
                    hangarPort.save(hangar.copy(etat = newState.getEtat()))
                } else {
                    Mono.just(hangar)
                }
            }
        }
}
