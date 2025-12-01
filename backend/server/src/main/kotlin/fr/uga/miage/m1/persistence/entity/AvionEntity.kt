package fr.uga.miage.m1.persistence.entity

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("avion")
data class AvionEntity(

    @Id
    val id: UUID? = null,

    val immatriculation: String,
    val type: String,
    val capacite: Int,
    val etat: AvionEtat,

    @Column("hangar_id")
    val hangarId: UUID? = null
)
