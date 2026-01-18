package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.VolDataPort
import fr.uga.miage.m1.domain.model.VolHistory
import fr.uga.miage.m1.domain.port.PisteDataPort
import fr.uga.miage.m1.domain.state.VolState
import fr.uga.miage.m1.domain.state.VolStateFactory
import fr.uga.miage.m1.domain.validation.VolValidator
import fr.uga.miage.m1.exceptions.NotFoundException
import fr.uga.miage.m1.models.VolDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class VolService(
    private val volPort: VolDataPort,
    private val avionPort: AvionDataPort,
    private val volHistoryService: VolHistoryService,
    private val pistePort: PisteDataPort,
    private val pisteService: PisteService,
    @Value("\${airport.code:ATL}") private val myAirportCode: String
) {

    fun list(): Flux<Vol> =
        volPort.findAll()

    fun get(id: UUID): Mono<Vol> =
        volPort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Vol $id non trouvé")))

    fun create(vol: Vol): Mono<Vol> {
        VolValidator.validate(vol)
        val toSave = vol.copy(etat = VolEtat.PREVU)

        return checkAvionAvailable(toSave)
            .flatMap { volPort.save(toSave) }
            .flatMap { saved ->
                logHistory(saved).thenReturn(saved)
            }
    }

    fun update(id: UUID, updated: Vol): Mono<Vol> =
        get(id).flatMap { current ->
            val stateChangeRequested = updated.etat != current.etat

            val merged = current.copy(
                numeroVol = updated.numeroVol,
                origine = updated.origine,
                destination = updated.destination,
                heureDepart = updated.heureDepart,
                heureArrivee = updated.heureArrivee,
                etat = current.etat,
                avionId = updated.avionId,
                pisteId = current.pisteId
            )

            VolValidator.validateForUpdate(merged)

            when {
                stateChangeRequested -> updateEtat(id, updated.etat)
                else -> volPort.save(merged)
                    .flatMap { saved ->
                        logHistory(saved).thenReturn(saved)
                    }
            }
        }

    fun updateBasicFields(id: UUID, updated: Vol): Mono<Vol> =
        get(id).flatMap { current ->
            val merged = current.copy(
                origine = updated.origine,
                destination = updated.destination,
                heureDepart = updated.heureDepart,
                heureArrivee = updated.heureArrivee
            )

            VolValidator.validate(merged)
            volPort.save(merged)
        }.flatMap { saved ->
            logHistory(saved).thenReturn(saved)
        }

    fun delete(id: UUID): Mono<Unit> =
        get(id).flatMap { vol ->
            if (vol.pisteId != null) {
                releasePisteInternal(vol)
                    .then(volPort.deleteById(id))
            } else {
                volPort.deleteById(id)
            }
        }.thenReturn(Unit)

    fun assignAvion(id: UUID, avionId: UUID): Mono<Vol> =
        Mono.zip(
            get(id),
            avionPort.findById(avionId)
                .switchIfEmpty(Mono.error(NotFoundException("Avion $avionId non trouvé")))
        ).flatMap { tuple ->
            val vol = tuple.t1
            val avion = tuple.t2

            if (avion.etat == AvionEtat.MAINTENANCE) {
                return@flatMap Mono.error<Vol>(
                    IllegalStateException("Impossible d'assigner un avion en maintenance")
                )
            }

            if (avion.etat != AvionEtat.DISPONIBLE) {
                return@flatMap Mono.error<Vol>(
                    IllegalStateException("L'avion doit être DISPONIBLE pour être assigné")
                )
            }

            volPort.save(vol.copy(avionId = avionId))
        }

    fun unassignAvion(id: UUID): Mono<Vol> =
        get(id).flatMap { vol ->
            val state = VolStateFactory.fromEtat(vol.etat)
            if (state.requiresAvion()) {
                Mono.error(
                    IllegalStateException(
                        "Impossible de retirer l'avion : le vol est en état ${vol.etat}"
                    )
                )
            } else {
                volPort.save(vol.copy(avionId = null))
            }
        }

    fun updateEtat(id: UUID, newEtat: VolEtat): Mono<Vol> =
        get(id).flatMap { current ->
            val currentState = VolStateFactory.fromEtat(current.etat)

            if (!currentState.canTransitionTo(newEtat)) {
                return@flatMap Mono.error<Vol>(
                    IllegalStateException(
                        "Transition de ${current.etat} vers $newEtat non autorisée"
                    )
                )
            }

            val newState = currentState.transitionTo(newEtat)

            if (newState.requiresAvion() && current.avionId == null) {
                return@flatMap Mono.error<Vol>(
                    IllegalStateException("Un avion doit être assigné pour passer à l'état $newEtat")
                )
            }

            handlePisteTransition(current, newState)
                .flatMap { updated ->
                    handleAvionStateTransition(updated, newState)
                }
                .flatMap { updated ->
                    volPort.save(updated.copy(etat = newState.getEtat()))
                }
                .flatMap { saved ->
                    logHistory(saved).thenReturn(saved)
                }
        }

    private fun handleAvionStateTransition(vol: Vol, newState: VolState): Mono<Vol> {
        if (vol.avionId == null) {
            return Mono.just(vol)
        }

        return when (newState.getEtat()) {
            VolEtat.DECOLLE -> {
                avionPort.findById(vol.avionId)
                    .flatMap { avion ->
                        avionPort.save(avion.copy(
                            etat = AvionEtat.EN_VOL,
                            hangarId = null
                        ))
                    }
                    .thenReturn(vol)
            }

            VolEtat.TERMINE -> {
                val isIncoming = vol.destination.equals(myAirportCode, ignoreCase = true)

                if (isIncoming) {
                    avionPort.findById(vol.avionId)
                        .flatMap { avion ->
                            avionPort.save(avion.copy(
                                etat = AvionEtat.MAINTENANCE,
                                hangarId = null
                            ))
                        }
                        .thenReturn(vol)
                } else {
                    Mono.just(vol)
                }
            }

            VolEtat.ANNULE -> {
                avionPort.findById(vol.avionId)
                    .flatMap { avion ->
                        if (avion.etat == AvionEtat.EN_VOL) {
                            avionPort.save(avion.copy(
                                etat = AvionEtat.MAINTENANCE,
                                hangarId = null
                            ))
                        } else {
                            Mono.just(avion)
                        }
                    }
                    .thenReturn(vol)
            }

            else -> Mono.just(vol)
        }
    }

    private fun handlePisteTransition(vol: Vol, newState: VolState): Mono<Vol> {
        val isOutgoing = vol.origine.equals(myAirportCode, ignoreCase = true)
        val isIncoming = vol.destination.equals(myAirportCode, ignoreCase = true)

        return when (newState.getEtat()) {
            VolEtat.DECOLLE -> {
                if (isOutgoing) {
                    if (vol.pisteId != null) {
                        occupyPiste(vol.pisteId!!).thenReturn(vol)
                    } else {
                        findAndAssignAvailablePiste(vol, isOutgoing = true)
                    }
                } else {
                    Mono.just(vol)
                }
            }
            VolEtat.EN_VOL -> {
                if (isOutgoing && vol.pisteId != null) {
                    releasePisteInternal(vol)
                } else {
                    Mono.just(vol)
                }
            }
            VolEtat.ARRIVE -> {
                if (isIncoming) {
                    if (vol.pisteId != null) {
                        occupyPiste(vol.pisteId!!).thenReturn(vol)
                    } else {
                        findAndAssignAvailablePiste(vol, isOutgoing = false)
                    }
                } else {
                    Mono.just(vol)
                }
            }
            VolEtat.TERMINE -> {
                if (isIncoming && vol.pisteId != null) {
                    releasePisteInternal(vol)
                } else {
                    Mono.just(vol)
                }
            }
            VolEtat.ANNULE -> {
                if (vol.pisteId != null) {
                    releasePisteInternal(vol)
                } else {
                    Mono.just(vol)
                }
            }
            else -> Mono.just(vol)
        }
    }

    private fun findAndAssignAvailablePiste(vol: Vol, isOutgoing: Boolean): Mono<Vol> =
        pistePort.findByEtat(PisteEtat.LIBRE)
            .next()
            .switchIfEmpty(
                Mono.error(
                    IllegalStateException(
                        if (isOutgoing) {
                            "Ce vol ne peut pas décoller pour le moment. Veuillez attendre qu'une piste se libère"
                        } else {
                            "Ce vol ne peut pas atterrir pour le moment. Veuillez attendre qu'une piste se libère"
                        }
                    )
                )
            )
            .flatMap { piste ->
                pisteService.updateEtat(piste.id!!, PisteEtat.OCCUPEE)
                    .thenReturn(vol.copy(pisteId = piste.id))
            }

    private fun occupyPiste(pisteId: UUID): Mono<Unit> =
        pisteService.updateEtat(pisteId, PisteEtat.OCCUPEE)
            .thenReturn(Unit)

    private fun releasePisteInternal(vol: Vol): Mono<Vol> =
        if (vol.pisteId != null) {
            pisteService.updateEtat(vol.pisteId!!, PisteEtat.LIBRE)
                .thenReturn(vol.copy(pisteId = null))
        } else {
            Mono.just(vol)
        }

    private fun checkAvionAvailable(vol: Vol): Mono<Boolean> {
        if (vol.avionId == null) return Mono.just(true)

        return avionPort.findById(vol.avionId)
            .switchIfEmpty(Mono.error(NotFoundException("Avion ${vol.avionId} non trouvé")))
            .flatMap { avion ->
                when (avion.etat) {
                    AvionEtat.MAINTENANCE ->
                        Mono.error(IllegalStateException("L'avion est en maintenance"))
                    AvionEtat.DISPONIBLE ->
                        Mono.just(true)
                    else ->
                        Mono.error(IllegalStateException("L'avion n'est pas disponible"))
                }
            }
    }

    fun listByEtat(etat: VolEtat): Flux<Vol> =
        volPort.findByEtat(etat)

    private fun logHistory(vol: Vol): Mono<VolHistory> =
        volHistoryService.save(
            VolHistory(volId = vol.id!!, etat = vol.etat)
        )

    fun listByPiste(pisteId: UUID): Flux<Vol> =
        volPort.findByPisteId(pisteId)

    fun listDeparturesFrom(airportCode: String): Flux<Vol> =
        volPort.findByOrigine(airportCode)

    fun listArrivalsTo(airportCode: String): Flux<Vol> =
        volPort.findByDestination(airportCode)

    fun trafficFor(airportCode: String): Flux<Vol> =
        volPort.findByOrigine(airportCode)
            .mergeWith(volPort.findByDestination(airportCode))

    fun findArrivalsToDestination(destination: String): Flux<VolDto> {
        return volPort.findAll()
            .filter { vol -> vol.destination.equals(destination, ignoreCase = true) }
            .flatMap { vol ->
                if (vol.avionId != null) {
                    avionPort.findById(vol.avionId)
                        .map { avion -> vol.toVolDto(avion.immatriculation) }
                        .defaultIfEmpty(vol.toVolDto(null))
                } else {
                    Mono.just(vol.toVolDto(null))
                }
            }
    }

    private fun Vol.toVolDto(avionImmatriculation: String?): VolDto {
        return VolDto(
            id = this.id.toString(),
            numeroVol = this.numeroVol,
            heureDepart = this.heureDepart,
            heureArrivee = this.heureArrivee,
            origine = this.origine,
            destination = this.destination,
            etat = this.etat.name,
            avionImmatriculation = avionImmatriculation,
            pisteId = this.pisteId?.toString()
        )
    }

}
