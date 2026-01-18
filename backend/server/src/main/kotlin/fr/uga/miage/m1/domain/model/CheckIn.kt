package fr.uga.miage.m1.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class CheckIn(
    val id: UUID? = null,
    val passagerId: UUID,
    val volId: UUID,
    val numeroSiege: String,
    val heureCheckIn: LocalDateTime = LocalDateTime.now()
)
