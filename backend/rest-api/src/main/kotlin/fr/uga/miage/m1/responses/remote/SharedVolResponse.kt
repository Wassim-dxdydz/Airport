package fr.uga.miage.m1.responses.remote

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import java.time.LocalDateTime

data class SharedVolResponse(
    val numeroVol: String,
    val origine: String,
    val destination: String,
    val heureDepart: LocalDateTime,
    val heureArrivee: LocalDateTime,
    val etat: VolEtat,

    val avionImmatriculation: String,
    val avionType: String,
    val avionCapacite: Int,
    val avionEtat: AvionEtat
)
