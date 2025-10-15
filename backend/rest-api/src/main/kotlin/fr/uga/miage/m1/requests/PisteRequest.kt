package fr.uga.miage.m1.requests

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CreatePisteRequest(
    @field:NotBlank val identifiant: String,
    @field:Min(1) val longueurM: Int,
    val etat: PisteEtat = PisteEtat.LIBRE
)

data class UpdatePisteEtatRequest(
    val etat: PisteEtat
)