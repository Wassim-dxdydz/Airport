package fr.uga.miage.m1.requests

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat

data class CreatePisteRequest(
    val identifiant: String,
    val longueurM: Int,
    val etat: PisteEtat = PisteEtat.LIBRE
)

data class UpdatePisteRequest(
    val identifiant: String?,
    val longueurM: Int?,
    val etat: PisteEtat?
)
