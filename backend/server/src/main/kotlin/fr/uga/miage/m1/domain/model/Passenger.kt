package fr.uga.miage.m1.domain.model

import java.util.UUID

data class Passenger(
    val id: UUID? = null,
    val prenom: String,
    val nom: String,
    val email: String,
    val telephone: String? = null
)
