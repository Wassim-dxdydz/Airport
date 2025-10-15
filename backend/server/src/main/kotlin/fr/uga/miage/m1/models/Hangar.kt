package fr.uga.miage.m1.models

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("hangar")
data class Hangar(
    @Id
    val id: UUID = UUID.randomUUID(),

    val identifiant: String,
    val capacite: Int,
    val etat: HangarEtat,

    @Column("created_at")
    val createdAt: LocalDateTime? = null, // filled by DB default NOW()
)
