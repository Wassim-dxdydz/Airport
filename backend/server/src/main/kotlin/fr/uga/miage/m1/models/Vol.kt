package fr.uga.miage.m1.models

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("vol")
data class Vol(
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

    @Column("created_at")
    val createdAt: LocalDateTime? = null
) {
    companion object {
        fun create(
            numeroVol: String,
            origine: String,
            destination: String,
            heureDepart: LocalDateTime,
            heureArrivee: LocalDateTime,
            etat: VolEtat = VolEtat.PREVU,
            avionId: UUID? = null
        ) = Vol(
            id = null,
            numeroVol = numeroVol,
            origine = origine,
            destination = destination,
            heureDepart = heureDepart,
            heureArrivee = heureArrivee,
            etat = etat,
            avionId = avionId,
            createdAt = null
        )
    }
}
