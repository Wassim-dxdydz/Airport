package fr.uga.miage.m1.requests

import java.util.UUID

data class CreateCheckInRequest(
    val passagerId: UUID,
    val volId: UUID,
    val numeroSiege: String
)

data class UpdateCheckInRequest(
    val numeroSiege: String? = null
)
