package fr.uga.miage.m1.responses

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import java.util.UUID

data class HangarResponse(
    val id: UUID,
    val identifiant: String,
    val capacite: Int,
    val etat: HangarEtat
)
