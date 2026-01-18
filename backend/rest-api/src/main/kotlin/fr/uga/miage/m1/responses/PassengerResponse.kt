package fr.uga.miage.m1.responses

import java.util.UUID

data class PassengerResponse(
    val id: UUID?,
    val prenom: String,
    val nom: String,
    val email: String,
    val telephone: String?
)
