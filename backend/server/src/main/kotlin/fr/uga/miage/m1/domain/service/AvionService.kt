package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.HangarDataPort
import fr.uga.miage.m1.domain.port.VolDataPort
import fr.uga.miage.m1.domain.state.AvionStateFactory
import fr.uga.miage.m1.domain.validation.AvionValidator
import fr.uga.miage.m1.exceptions.*
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.UUID

@Service
class AvionService(
    private val avionPort: AvionDataPort,
    private val hangarPort: HangarDataPort,
    private val volPort: VolDataPort,
    @Lazy private val hangarService: HangarService
) {

    fun list(): Flux<Avion> =
        avionPort.findAll()

    fun get(id: UUID): Mono<Avion> =
        avionPort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Avion $id non trouvé")))

    fun create(avion: Avion): Mono<Avion> {
        if (avion.etat == AvionEtat.EN_VOL) {
            return Mono.error(
                IllegalStateException("Un avion ne peut pas être créé directement avec l'état EN_VOL")
            )
        }
        val normalized = enforceHangarConsistency(
            avion.copy(immatriculation = avion.immatriculation.trim().uppercase())
        )
        AvionValidator.validate(normalized)

        return avionPort.existsByImmatriculation(normalized.immatriculation)
            .flatMap { exists ->
                if (exists) {
                    Mono.error(IllegalStateException("L'immatriculation ${normalized.immatriculation} est déjà utilisée"))
                } else {
                    checkHangarExists(normalized.hangarId)
                        .flatMap {
                            if (normalized.hangarId != null) {
                                hangarService.ensureCanAcceptAvion(normalized.hangarId)
                            } else {
                                Mono.just(Unit)
                            }
                        }
                        .flatMap { avionPort.save(normalized) }
                        .flatMap { savedAvion ->
                            if (savedAvion.hangarId != null) {
                                hangarService.updateStateBasedOnOccupancy(savedAvion.hangarId)
                                    .thenReturn(savedAvion)
                            } else {
                                Mono.just(savedAvion)
                            }
                        }
                }
            }
    }

    fun update(id: UUID, avion: Avion): Mono<Avion> =
        get(id).flatMap { current ->
            if (current.etat == AvionEtat.EN_VOL) {
                return@flatMap Mono.error(
                    IllegalStateException("Impossible de modifier un avion en état EN_VOL")
                )
            }

            val mergedRaw = current.copy(
                immatriculation = avion.immatriculation,
                type = avion.type,
                capacite = avion.capacite,
                etat = avion.etat,
                hangarId = current.hangarId
            )

            val merged = enforceHangarConsistency(mergedRaw)
            AvionValidator.validate(merged)

            if (merged.etat == current.etat) {
                avionPort.save(merged)
            } else {
                hasActiveFlights(id).flatMap { active ->
                    val nextEtat = AvionStateFactory
                        .fromEtat(current.etat)
                        .transitionTo(merged.etat, hasActiveFlights = active)
                        .getEtat()
                    avionPort.save(merged.copy(etat = nextEtat))
                }
            }
        }

    fun delete(id: UUID): Mono<Unit> =
        get(id).flatMap { avion ->
            hasActiveFlights(id).flatMap { locked ->
                if (locked) {
                    Mono.error<Unit>(
                        IllegalStateException("Impossible de supprimer l'avion $id : il a des vols actifs")
                    )
                } else {
                    val hangarId = avion.hangarId
                    avionPort.deleteById(id)
                        .flatMap {
                            if (hangarId != null) {
                                hangarService.updateStateBasedOnOccupancy(hangarId)
                                    .thenReturn(Unit)
                            } else {
                                Mono.just(Unit)
                            }
                        }
                }
            }
        }

    fun assignHangar(id: UUID, hangarId: UUID): Mono<Avion> =
        hangarService.ensureCanAcceptAvion(hangarId)
            .flatMap { get(id) }
            .flatMap { avion ->
                val state = AvionStateFactory.fromEtat(avion.etat)
                if (!state.canAssignHangar()) {
                    Mono.error(
                        IllegalStateException("Impossible d'assigner un hangar si l'avion est ${avion.etat}")
                    )
                } else {
                    val oldHangarId = avion.hangarId
                    avionPort.save(avion.copy(hangarId = hangarId))
                        .flatMap { savedAvion ->
                            hangarService.updateStateBasedOnOccupancy(hangarId)
                                .flatMap {
                                    if (oldHangarId != null) {
                                        hangarService.updateStateBasedOnOccupancy(oldHangarId)
                                            .thenReturn(savedAvion)
                                    } else {
                                        Mono.just(savedAvion)
                                    }
                                }
                        }
                }
            }

    fun unassignHangar(id: UUID): Mono<Avion> =
        get(id).flatMap { avion ->
            if (avion.etat == AvionEtat.DISPONIBLE) {
                Mono.error(IllegalStateException("Impossible de retirer le hangar : l'avion est DISPONIBLE et doit être garé"))
            } else {
                val oldHangarId = avion.hangarId
                avionPort.save(avion.copy(hangarId = null))
                    .flatMap { savedAvion ->
                        if (oldHangarId != null) {
                            hangarService.updateStateBasedOnOccupancy(oldHangarId)
                                .thenReturn(savedAvion)
                        } else {
                            Mono.just(savedAvion)
                        }
                    }
            }
        }

    private fun checkHangarExists(hangarId: UUID?): Mono<Boolean> =
        if (hangarId == null) {
            Mono.just(true)
        } else {
            hangarPort.existsById(hangarId)
                .flatMap { exists ->
                    if (exists) Mono.just(true)
                    else Mono.error(NotFoundException("Hangar $hangarId non trouvé"))
                }
        }

    private val ACTIVE_FLIGHT_STATES = setOf(
        VolEtat.PREVU,
        VolEtat.EN_ATTENTE,
        VolEtat.EMBARQUEMENT,
        VolEtat.DECOLLE,
        VolEtat.EN_VOL
    )

    private fun hasActiveFlights(avionId: UUID): Mono<Boolean> =
        volPort.existsByAvionIdAndEtatIn(avionId, ACTIVE_FLIGHT_STATES)

    private fun enforceHangarConsistency(avion: Avion): Avion =
        when (avion.etat) {
            AvionEtat.EN_VOL ->
                avion.copy(hangarId = null)

            AvionEtat.DISPONIBLE -> {
                if (avion.hangarId == null) {
                    throw IllegalStateException("Un avion DISPONIBLE doit être assigné à un hangar")
                }
                avion
            }

            AvionEtat.MAINTENANCE ->
                avion
        }
}
