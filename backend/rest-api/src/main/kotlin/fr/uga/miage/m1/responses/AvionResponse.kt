package fr.uga.miage.m1.responses

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import java.util.UUID

data class AvionResponse(
    val id: UUID?,
    val immatriculation: String,
    val type: String,
    val capacite: Int,
    val etat: AvionEtat,
    val hangarId: UUID?
)