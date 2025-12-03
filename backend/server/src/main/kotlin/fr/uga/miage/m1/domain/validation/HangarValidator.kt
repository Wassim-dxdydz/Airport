package fr.uga.miage.m1.domain.validation

import fr.uga.miage.m1.domain.model.Hangar

object HangarValidator {

    // Format réaliste : H1, H12, A3, B04, G7, etc.
    private val identifiantRegex = Regex("^[A-Z][A-Z0-9]{0,3}$")

    fun validate(h: Hangar) {
        validateIdentifiant(h)
        validateCapacity(h)
    }
    // On vérifie que l'identifiant du hangar est valide
    private fun validateIdentifiant(h: Hangar) {
        // On vérifie que l'identifiant n'est pas vide
        if (h.identifiant.isBlank())
            throw IllegalArgumentException("L'identifiant du hangar ne peut pas être vide.")
        // On vérifie que l'identifiant respecte le format attendu
        if (!identifiantRegex.matches(h.identifiant.uppercase()))
            throw IllegalArgumentException(
                "Identifiant hangar '${h.identifiant}' invalide (ex: H1, H12, A3, B04)."
            )
    }
    // On vérifie que la capacité du hangar est > 0
    private fun validateCapacity(h: Hangar) {
        if (h.capacite <= 0)
            throw IllegalArgumentException("La capacité du hangar doit être > 0.")
    }
}