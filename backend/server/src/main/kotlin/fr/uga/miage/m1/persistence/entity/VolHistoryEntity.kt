package fr.uga.miage.m1.persistence.entity

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("vol_history")
data class VolHistoryEntity(

    @Id
    val id: UUID? = null,

    @Column("vol_id")
    val volId: UUID,

    @Column("etat")
    val etat: VolEtat,

    @Column("changed_at")
    val changedAt: LocalDateTime = LocalDateTime.now()
)
