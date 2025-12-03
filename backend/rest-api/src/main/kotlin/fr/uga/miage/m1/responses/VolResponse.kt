package fr.uga.miage.m1.responses

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import java.time.LocalDateTime
import java.util.UUID

data class VolResponse(
    val id: UUID?,
    val numeroVol: String,
    val origine: String,
    val destination: String,
    val heureDepart: LocalDateTime,
    val heureArrivee: LocalDateTime,
    val etat: VolEtat,
    val avionId: UUID?,
    val pisteId: UUID?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
