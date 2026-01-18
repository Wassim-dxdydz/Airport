package fr.uga.miage.m1.domain.service

import fr.uga.miage.m1.domain.model.Passenger
import fr.uga.miage.m1.domain.port.PassengerDataPort
import fr.uga.miage.m1.domain.port.CheckInDataPort
import fr.uga.miage.m1.domain.validation.PassengerValidator
import fr.uga.miage.m1.exceptions.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class PassengerService(
    private val passengerPort: PassengerDataPort,
    private val checkInPort: CheckInDataPort
) {

    fun list(): Flux<Passenger> = passengerPort.findAll()

    fun get(id: UUID): Mono<Passenger> =
        passengerPort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Passager $id non trouvé")))

    fun create(passenger: Passenger): Mono<Passenger> {
        PassengerValidator.validate(passenger)

        return passengerPort.existsByEmail(passenger.email)
            .flatMap { exists ->
                if (exists) {
                    Mono.error(IllegalStateException("Un passager avec cet email existe déjà"))
                } else {
                    passengerPort.save(passenger)
                }
            }
    }

    fun update(id: UUID, updated: Passenger): Mono<Passenger> =
        get(id).flatMap { current ->
            val merged = current.copy(
                prenom = updated.prenom,
                nom = updated.nom,
                email = updated.email,
                telephone = updated.telephone
            )

            PassengerValidator.validateForUpdate(merged)

            if (current.email != merged.email) {
                passengerPort.existsByEmail(merged.email)
                    .flatMap { exists ->
                        if (exists) {
                            Mono.error(IllegalStateException("Un passager avec cet email existe déjà"))
                        } else {
                            passengerPort.save(merged)
                        }
                    }
            } else {
                passengerPort.save(merged)
            }
        }

    fun delete(id: UUID): Mono<Unit> =
        get(id).flatMap { passenger ->
            checkInPort.findByPassagerId(id)
                .hasElements()
                .flatMap { hasCheckIns ->
                    if (hasCheckIns) {
                        Mono.error(
                            IllegalStateException(
                                "Impossible de supprimer ce passager: " +
                                        "il a des enregistrements de check-in"
                            )
                        )
                    } else {
                        passengerPort.deleteById(id)
                    }
                }
        }.thenReturn(Unit)

    fun listNotCheckedInForVol(volId: UUID): Flux<Passenger> =
        passengerPort.findAll()
            .filterWhen { passenger ->
                checkInPort.existsByVolIdAndPassagerId(volId, passenger.id!!)
                    .map { !it }
            }
}
