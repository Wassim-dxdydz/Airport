package fr.uga.miage.m1.requests

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

data class CreateVolRequest(
    @field:NotBlank val numeroVol: String,
    @field:NotBlank val origine: String,
    @field:NotBlank val destination: String,
    @field:NotNull @field:Future val heureDepart: LocalDateTime,
    @field:NotNull @field:Future val heureArrivee: LocalDateTime
)

data class UpdateVolRequest(
    val origine: String? = null,
    val destination: String? = null,
    @field:Future val heureDepart: LocalDateTime? = null,
    @field:Future val heureArrivee: LocalDateTime? = null,
    val etat: VolEtat? = null,
    val avionId: UUID? = null
)
