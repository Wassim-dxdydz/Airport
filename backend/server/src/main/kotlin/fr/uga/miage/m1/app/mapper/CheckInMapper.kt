package fr.uga.miage.m1.app.mapper

import fr.uga.miage.m1.domain.model.CheckIn
import fr.uga.miage.m1.requests.CreateCheckInRequest
import fr.uga.miage.m1.requests.UpdateCheckInRequest
import fr.uga.miage.m1.responses.CheckInResponse
import fr.uga.miage.m1.responses.PassengerResponse
import java.time.LocalDateTime

object CheckInMapper {
    fun toDomain(req: CreateCheckInRequest): CheckIn =
        CheckIn(
            id = null,
            passagerId = req.passagerId,
            volId = req.volId,
            numeroSiege = req.numeroSiege.trim().uppercase(),
            heureCheckIn = LocalDateTime.now()
        )

    fun toUpdatedDomain(current: CheckIn, req: UpdateCheckInRequest): CheckIn =
        CheckIn(
            id = current.id,
            passagerId = current.passagerId,
            volId = current.volId,
            numeroSiege = req.numeroSiege?.trim()?.uppercase() ?: current.numeroSiege,
            heureCheckIn = current.heureCheckIn
        )

    fun toResponse(
        checkIn: CheckIn,
        passagerInfo: PassengerResponse? = null
    ): CheckInResponse =
        CheckInResponse(
            id = checkIn.id,
            passagerId = checkIn.passagerId,
            volId = checkIn.volId,
            numeroSiege = checkIn.numeroSiege,
            heureCheckIn = checkIn.heureCheckIn,
            passagerInfo = passagerInfo
        )
}
