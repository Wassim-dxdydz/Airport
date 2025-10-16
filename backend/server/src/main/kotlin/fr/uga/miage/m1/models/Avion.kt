package fr.uga.miage.m1.models

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("avion")
data class Avion(
    @Id
    val id: UUID? = null,

    val immatriculation: String,
    val type: String,
    val capacite: Int,
    val etat: AvionEtat,

    @Column("hangar_id")
    val hangarId: UUID? = null,

    @Column("created_at")
    val createdAt: LocalDateTime? = null
) {
    companion object {
        fun create(
            immatriculation: String,
            type: String,
            capacite: Int,
            etat: AvionEtat,
            hangarId: UUID? = null
        ) = Avion(
            id = null,
            immatriculation = immatriculation,
            type = type,
            capacite = capacite,
            etat = etat,
            hangarId = hangarId,
            createdAt = null
        )
    }
}
