package fr.uga.miage.m1.persistence.entity

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.InsertOnlyProperty
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("vol")
data class VolEntity(
    @Id
    val id: UUID? = null,

    @Column("numero_vol")
    val numeroVol: String,

    val origine: String,
    val destination: String,

    @Column("heure_depart")
    val heureDepart: LocalDateTime,

    @Column("heure_arrivee")
    val heureArrivee: LocalDateTime,

    val etat: VolEtat,

    @Column("avion_id")
    val avionId: UUID? = null,

    @Column("piste_id")
    val pisteId: UUID? = null,

    @InsertOnlyProperty
    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime? = null
)
