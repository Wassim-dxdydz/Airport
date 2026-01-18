package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.CheckIn
import fr.uga.miage.m1.domain.port.CheckInDataPort
import fr.uga.miage.m1.domain.port.PassengerDataPort
import fr.uga.miage.m1.domain.port.VolDataPort
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.validation.CheckInValidator
import fr.uga.miage.m1.exceptions.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class CheckInService(
    private val checkInPort: CheckInDataPort,
    private val passengerPort: PassengerDataPort,
    private val volPort: VolDataPort,
    private val avionPort: AvionDataPort
) {

    fun list(): Flux<CheckIn> = checkInPort.findAll()

    fun get(id: UUID): Mono<CheckIn> =
        checkInPort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Check-in $id non trouvé")))

    fun listByVol(volId: UUID): Flux<CheckIn> =
        volPort.existsById(volId)
            .flatMapMany { exists ->
                if (!exists) {
                    Flux.error(NotFoundException("Vol $volId non trouvé"))
                } else {
                    checkInPort.findByVolId(volId)
                }
            }

    fun listByPassager(passagerId: UUID): Flux<CheckIn> =
        passengerPort.existsById(passagerId)
            .flatMapMany { exists ->
                if (!exists) {
                    Flux.error(NotFoundException("Passager $passagerId non trouvé"))
                } else {
                    checkInPort.findByPassagerId(passagerId)
                }
            }

    fun create(checkIn: CheckIn): Mono<CheckIn> {
        CheckInValidator.validate(checkIn)

        return passengerPort.existsById(checkIn.passagerId)
            .flatMap { passengerExists ->
                if (!passengerExists) {
                    return@flatMap Mono.error<CheckIn>(
                        NotFoundException("Passager ${checkIn.passagerId} non trouvé")
                    )
                }

                volPort.findById(checkIn.volId)
                    .switchIfEmpty(Mono.error(NotFoundException("Vol ${checkIn.volId} non trouvé")))
                    .flatMap { vol ->
                        if (!canCheckIn(vol.etat, vol.avionId)) {
                            return@flatMap Mono.error<CheckIn>(
                                IllegalStateException(
                                    "Impossible de s'enregistrer sur ce vol. " +
                                            "Le vol doit avoir un avion assigné et être en statut PREVU ou EMBARQUEMENT. " +
                                            "Statut actuel: ${vol.etat}, Avion assigné: ${vol.avionId != null}"
                                )
                            )
                        }

                        avionPort.findById(vol.avionId!!)
                            .switchIfEmpty(Mono.error(IllegalStateException("Avion ${vol.avionId} introuvable")))
                            .flatMap { avion ->
                                val validationError = CheckInValidator.validateSeatForCapacity(
                                    checkIn.numeroSiege,
                                    avion.capacite
                                )
                                if (validationError != null) {
                                    return@flatMap Mono.error<CheckIn>(IllegalArgumentException(validationError))
                                }

                                // FIX: Chaîner correctement avec .flatMap
                                checkInPort.existsByVolIdAndNumeroSiege(checkIn.volId, checkIn.numeroSiege)
                                    .flatMap { seatTaken ->
                                        if (seatTaken) {
                                            return@flatMap Mono.error<CheckIn>(
                                                IllegalStateException(
                                                    "Le siège ${checkIn.numeroSiege} est déjà occupé sur ce vol"
                                                )
                                            )
                                        }

                                        checkInPort.existsByVolIdAndPassagerId(checkIn.volId, checkIn.passagerId)
                                            .flatMap { alreadyCheckedIn ->
                                                if (alreadyCheckedIn) {
                                                    Mono.error(
                                                        IllegalStateException(
                                                            "Le passager est déjà enregistré sur ce vol"
                                                        )
                                                    )
                                                } else {
                                                    checkInPort.save(checkIn)
                                                }
                                            }
                                    }
                            }
                    }
            }
    }

    fun update(id: UUID, updated: CheckIn): Mono<CheckIn> =
        get(id).flatMap { current ->
            val merged = current.copy(
                numeroSiege = updated.numeroSiege
            )

            CheckInValidator.validate(merged)

            volPort.findById(current.volId)
                .flatMap { vol ->
                    if (!canCheckIn(vol.etat, vol.avionId)) {
                        return@flatMap Mono.error<CheckIn>(
                            IllegalStateException(
                                "Impossible de modifier l'enregistrement. " +
                                        "Le vol doit avoir un avion assigné et être en statut PREVU ou EMBARQUEMENT"
                            )
                        )
                    }

                    if (current.numeroSiege != merged.numeroSiege) {
                        avionPort.findById(vol.avionId!!)
                            .flatMap { avion ->
                                val validationError = CheckInValidator.validateSeatForCapacity(
                                    merged.numeroSiege,
                                    avion.capacite
                                )
                                if (validationError != null) {
                                    return@flatMap Mono.error<CheckIn>(IllegalArgumentException(validationError))
                                }

                                // FIX: Chaîner correctement avec .flatMap
                                checkInPort.existsByVolIdAndNumeroSiege(vol.id!!, merged.numeroSiege)
                                    .flatMap { seatTaken ->
                                        if (seatTaken) {
                                            Mono.error<CheckIn>(
                                                IllegalStateException(
                                                    "Le siège ${merged.numeroSiege} est déjà occupé sur ce vol"
                                                )
                                            )
                                        } else {
                                            checkInPort.save(merged)
                                        }
                                    }
                            }
                    } else {
                        checkInPort.save(merged)
                    }
                }
        }

    fun delete(id: UUID): Mono<Unit> =
        get(id).flatMap { checkIn ->
            volPort.findById(checkIn.volId)
                .flatMap { vol ->
                    if (!canCheckIn(vol.etat, vol.avionId)) {
                        Mono.error<Unit>(
                            IllegalStateException(
                                "Impossible d'annuler l'enregistrement pour un vol avec le statut ${vol.etat}"
                            )
                        )
                    } else {
                        checkInPort.deleteById(id).then(Mono.just(Unit))
                    }
                }
        }

    fun verifyPassengerCheckIn(volId: UUID, passagerId: UUID): Mono<Boolean> =
        checkInPort.existsByVolIdAndPassagerId(volId, passagerId)

    private fun canCheckIn(etat: VolEtat, avionId: UUID?): Boolean {
        return avionId != null && (etat == VolEtat.PREVU || etat == VolEtat.EMBARQUEMENT)
    }
}
