package fr.uga.miage.m1.responses

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import java.time.LocalDateTime
import java.util.UUID

data class VolHistoryResponse(
    val id: UUID?,
    val etat: VolEtat,
    val changedAt: LocalDateTime
)

