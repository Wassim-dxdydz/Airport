package fr.uga.miage.m1.domain.validation

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.domain.model.Piste

object PisteValidator {
    private val identifiantRegex = Regex("^([0-3][0-9][LCR]?|[A-Z][0-9]{1,2})$")

    private const val MIN_LONGUEUR_M = 825

    fun validate(p: Piste) {
        validateIdentifiant(p)
        validateLongueur(p)
        validateEtat(p)
    }

    private fun validateIdentifiant(p: Piste) {
        if (p.identifiant.isBlank())
            throw IllegalArgumentException("L'identifiant de la piste ne peut pas être vide.")

        if (!identifiantRegex.matches(p.identifiant.uppercase()))
            throw IllegalArgumentException(
                "Identifiant de piste '${p.identifiant}' invalide. " +
                        "Exemples valides : 09L, 27R, 18, A1, H2"
            )
    }

    private fun validateLongueur(p: Piste) {
        if (p.longueurM <= 0)
            throw IllegalArgumentException("La longueur de la piste doit être > 0.")

        if (p.longueurM < MIN_LONGUEUR_M)
            throw IllegalArgumentException(
                "La longueur de la piste doit être au moins ${MIN_LONGUEUR_M}m. " +
                        "Longueur fournie : ${p.longueurM}m"
            )
    }

    private fun validateEtat(p: Piste) {
        if (p.etat !in PisteEtat.entries.toTypedArray()) {
            throw IllegalArgumentException(
                "État de la piste invalide : ${p.etat}"
            )
        }
    }
}
