package fr.uga.miage.m1.domain.model

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import java.util.UUID

data class Piste(
    val id: UUID?,
    val identifiant: String,
    val longueurM: Int,
    val etat: PisteEtat
)
