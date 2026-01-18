package fr.uga.miage.m1.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("passagers")
data class PassengerEntity(
    @Id
    val id: UUID? = null,

    @Column("prenom")
    val prenom: String,

    @Column("nom")
    val nom: String,

    @Column("email")
    val email: String,

    @Column("telephone")
    val telephone: String? = null
)
