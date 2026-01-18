package fr.uga.miage.m1.requests

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat

data class CreateHangarRequest(
    val identifiant: String,
    val capacite: Int,
    val etat: HangarEtat = HangarEtat.DISPONIBLE
)

data class UpdateHangarRequest(
    val capacite: Int? = null,
    val etat: HangarEtat? = null
)