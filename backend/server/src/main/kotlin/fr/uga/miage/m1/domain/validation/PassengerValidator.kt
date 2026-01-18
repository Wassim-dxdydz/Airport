package fr.uga.miage.m1.domain.validation

import fr.uga.miage.m1.domain.model.Passenger

object PassengerValidator {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    private val phoneRegex = Regex("^\\+?[0-9]{10,15}\$")

    fun validate(passenger: Passenger) {
        validatePrenom(passenger.prenom)
        validateNom(passenger.nom)
        validateEmail(passenger.email)
        passenger.telephone?.let { validateTelephone(it) }
    }

    fun validateForUpdate(passenger: Passenger) {
        validate(passenger)
    }

    private fun validatePrenom(prenom: String) {
        if (prenom.isBlank()) {
            throw IllegalArgumentException("Le prénom ne peut pas être vide")
        }
        if (prenom.length < 2) {
            throw IllegalArgumentException("Le prénom doit contenir au moins 2 caractères")
        }
        if (!prenom.matches(Regex("^[a-zA-ZÀ-ÿ\\s-]+\$"))) {
            throw IllegalArgumentException("Le prénom ne peut contenir que des lettres, espaces et traits d'union")
        }
    }

    private fun validateNom(nom: String) {
        if (nom.isBlank()) {
            throw IllegalArgumentException("Le nom ne peut pas être vide")
        }
        if (nom.length < 2) {
            throw IllegalArgumentException("Le nom doit contenir au moins 2 caractères")
        }
        if (!nom.matches(Regex("^[a-zA-ZÀ-ÿ\\s-]+\$"))) {
            throw IllegalArgumentException("Le nom ne peut contenir que des lettres, espaces et traits d'union")
        }
    }

    private fun validateEmail(email: String) {
        if (email.isBlank()) {
            throw IllegalArgumentException("L'email ne peut pas être vide")
        }
        if (!emailRegex.matches(email)) {
            throw IllegalArgumentException(
                "Format d'email invalide. " +
                        "Exemple valide: utilisateur@domaine.com"
            )
        }
    }

    private fun validateTelephone(telephone: String) {
        if (telephone.isNotBlank() && !phoneRegex.matches(telephone)) {
            throw IllegalArgumentException(
                "Format de téléphone invalide. " +
                        "Le numéro doit contenir entre 10 et 15 chiffres, " +
                        "avec un + optionnel au début. " +
                        "Exemples: +33612345678, 0612345678"
            )
        }
    }
}