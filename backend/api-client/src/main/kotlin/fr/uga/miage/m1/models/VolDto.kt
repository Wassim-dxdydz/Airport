package fr.uga.miage.m1.models

import java.time.LocalDateTime

data class VolDto(
    val id: String,
    val numeroVol: String,
    val heureDepart: LocalDateTime,
    val heureArrivee: LocalDateTime,
    val origine: String,
    val destination: String,
    val etat: String,
    val avionImmatriculation: String?,
    val pisteId: String?
)
