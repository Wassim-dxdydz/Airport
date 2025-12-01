package fr.uga.miage.m1.persistence.entity

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("hangar")
data class HangarEntity(
    @Id
    val id: UUID? = null,
    val identifiant: String,
    val capacite: Int,
    val etat: HangarEtat
)
