package fr.uga.miage.m1.requests

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class CreateAvionRequest(
    @field:NotBlank val immatriculation: String,
    @field:NotBlank val type: String,
    @field:Min(1) val capacite: Int,
    val etat: AvionEtat = AvionEtat.EN_SERVICE,
    val hangarId: UUID? = null
)

data class UpdateAvionRequest(
    val type: String? = null,
    @field:Min(1) val capacite: Int? = null,
    val etat: AvionEtat? = null,
    val hangarId: UUID? = null
)


