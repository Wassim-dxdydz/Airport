package fr.uga.miage.m1.domain.validation

import fr.uga.miage.m1.domain.model.Avion

object AvionValidator {

    // Immatriculations françaises, européennes, internationales, FAA (USA)
    private val immatRegex = Regex("^([A-Z]{1,2}-[A-Z0-9]{3,5}|N[0-9A-Z]{1,5})$")

    fun validate(avion: Avion) {
        validateImmatriculation(avion)
        validateType(avion)
        validateCapacite(avion)
    }

    private fun validateImmatriculation(avion: Avion) {
        //On vérifie que l'immatriculation n'est pas vide
        if (avion.immatriculation.isBlank())
            throw IllegalArgumentException("L'immatriculation ne peut pas être vide.")
        //On vérifie que l'immatriculation respecte le format attendu
        if (!immatRegex.matches(avion.immatriculation.uppercase()))
            throw IllegalArgumentException(
                "Format d'immatriculation invalide : '${avion.immatriculation}'. " +
                        "Exemples valides : F-BZHE, HB-JCA, D-ABCD, N12345"
            )
    }
    // On vérifie que le type d'avion n'est pas vide
    private fun validateType(avion: Avion) {
        if (avion.type.isBlank())
            throw IllegalArgumentException("Le type d'avion ne peut pas être vide.")
    }
    // On vérifie que la capacité est un entier > 0
    private fun validateCapacite(avion: Avion) {
        if (avion.capacite <= 0)
            throw IllegalArgumentException("La capacité doit être un entier > 0.")
    }
}