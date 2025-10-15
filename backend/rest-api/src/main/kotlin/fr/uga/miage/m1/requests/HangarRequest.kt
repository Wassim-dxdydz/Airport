package fr.uga.miage.m1.requests

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CreateHangarRequest(
    @field:NotBlank val identifiant: String,
    @field:Min(0) val capacite: Int,
    val etat: HangarEtat = HangarEtat.DISPONIBLE
)

data class UpdateHangarRequest(
    @field:Min(0) val capacite: Int? = null,
    val etat: HangarEtat? = null
)