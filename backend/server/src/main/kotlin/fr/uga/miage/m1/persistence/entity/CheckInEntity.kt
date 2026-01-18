package fr.uga.miage.m1.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("checkins")
data class CheckInEntity(
    @Id
    val id: UUID? = null,

    @Column("passager_id")
    val passagerId: UUID,

    @Column("vol_id")
    val volId: UUID,

    @Column("numero_siege")
    val numeroSiege: String,

    @Column("heure_checkin")
    val heureCheckIn: LocalDateTime
)
