package fr.uga.miage.m1.persistence.entity

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("piste")
data class PisteEntity(
    @Id
    val id: UUID? = null,

    val identifiant: String,

    @Column("longueur_m")
    val longueurM: Int,

    val etat: PisteEtat
)
