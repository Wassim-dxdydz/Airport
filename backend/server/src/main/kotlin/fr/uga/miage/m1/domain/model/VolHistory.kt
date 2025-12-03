package fr.uga.miage.m1.domain.model

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import java.time.LocalDateTime
import java.util.UUID

data class VolHistory(
    val id: UUID? = null,
    val volId: UUID,
    val etat: VolEtat,
    val changedAt: LocalDateTime = LocalDateTime.now()
)
