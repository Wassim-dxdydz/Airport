package fr.uga.miage.m1.requests

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import java.util.UUID

data class CreateAvionRequest(
    val immatriculation: String,
    val type: String,
    val capacite: Int,
    val etat: AvionEtat = AvionEtat.EN_VOL,
    val hangarId: UUID? = null
)

data class UpdateAvionRequest(
    val type: String? = null,
    val capacite: Int? = null,
    val etat: AvionEtat? = null
)


