package fr.uga.miage.m1.requests

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import java.time.LocalDateTime
import java.util.UUID

data class CreateVolRequest(
    val numeroVol: String,
    val origine: String,
    val destination: String,
    val heureDepart: LocalDateTime,
    val heureArrivee: LocalDateTime,
    val avionId: UUID
)

data class UpdateVolRequest(
    val origine: String? = null,
    val destination: String? = null,
    val heureDepart: LocalDateTime? = null,
    val heureArrivee: LocalDateTime? = null,
    val etat: VolEtat? = null
)
