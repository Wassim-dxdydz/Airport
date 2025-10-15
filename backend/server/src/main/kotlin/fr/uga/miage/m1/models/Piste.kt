package fr.uga.miage.m1.models

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("piste")
data class Piste(
    @Id
    val id: UUID = UUID.randomUUID(),

    val identifiant: String,
    @Column("longueur_m")
    val longueurM: Int,
    val etat: PisteEtat,

    @Column("created_at")
    val createdAt: LocalDateTime? = null,
)
