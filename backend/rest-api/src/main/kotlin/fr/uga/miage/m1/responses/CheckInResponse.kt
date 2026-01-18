package fr.uga.miage.m1.responses

import java.time.LocalDateTime
import java.util.UUID

data class CheckInResponse(
    val id: UUID?,
    val passagerId: UUID,
    val volId: UUID,
    val numeroSiege: String,
    val heureCheckIn: LocalDateTime,
    val passagerInfo: PassengerResponse? = null
)
