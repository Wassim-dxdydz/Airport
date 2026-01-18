package fr.uga.miage.m1.requests

data class CreatePassengerRequest(
    val prenom: String,
    val nom: String,
    val email: String,
    val telephone: String? = null
)

data class UpdatePassengerRequest(
    val prenom: String? = null,
    val nom: String? = null,
    val email: String? = null,
    val telephone: String? = null
)
