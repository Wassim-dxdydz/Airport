package fr.uga.miage.m1.app.mapper

import fr.uga.miage.m1.domain.model.Passenger
import fr.uga.miage.m1.requests.CreatePassengerRequest
import fr.uga.miage.m1.requests.UpdatePassengerRequest
import fr.uga.miage.m1.responses.PassengerResponse

object PassengerMapper {
    fun toDomain(req: CreatePassengerRequest): Passenger =
        Passenger(
            id = null,
            prenom = req.prenom.trim(),
            nom = req.nom.trim(),
            email = req.email.trim().lowercase(),
            telephone = req.telephone?.trim()
        )

    fun toUpdatedDomain(current: Passenger, req: UpdatePassengerRequest): Passenger =
        Passenger(
            id = current.id,
            prenom = req.prenom?.trim() ?: current.prenom,
            nom = req.nom?.trim() ?: current.nom,
            email = req.email?.trim()?.lowercase() ?: current.email,
            telephone = req.telephone?.trim() ?: current.telephone
        )

    fun toResponse(passenger: Passenger): PassengerResponse =
        PassengerResponse(
            id = passenger.id,
            prenom = passenger.prenom,
            nom = passenger.nom,
            email = passenger.email,
            telephone = passenger.telephone
        )
}
