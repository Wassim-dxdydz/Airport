package fr.uga.miage.m1.domain.validation

import fr.uga.miage.m1.domain.model.Piste

object PisteValidator {
    // Identifiant: 1 à 4 caractères alphanumériques en majuscules (ex: 09L, 12, A1)
    private val identifiantRegex = Regex("^[A-Z0-9]{1,4}$")
    // On vérifie que l'identifiant et la longueur de la piste sont valides
    fun validate(p: Piste) {
        validateIdentifiant(p)
        validateLongueur(p)
    }
    // On vérifie que l'identifiant de la piste est valide
    private fun validateIdentifiant(p: Piste) {
        if (!identifiantRegex.matches(p.identifiant.uppercase()))
            throw IllegalArgumentException("Identifiant de piste '${p.identifiant}' invalide (ex: 09L, 12, A1).")
    }
    // On vérifie que la longueur de la piste est > 0
    private fun validateLongueur(p: Piste) {
        if (p.longueurM <= 0)
            throw IllegalArgumentException("La longueur de la piste doit être > 0.")
    }
}