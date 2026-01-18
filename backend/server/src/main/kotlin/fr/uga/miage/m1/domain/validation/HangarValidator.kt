package fr.uga.miage.m1.domain.validation

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.domain.model.Hangar

object HangarValidator {

    // Format réaliste : H1, H12, A3, B04, G7, etc.
    private val identifiantRegex = Regex("^[A-Z][A-Z0-9]{0,3}$")

    fun validate(h: Hangar) {
        validateIdentifiant(h)
        validateCapacity(h)
        validateEtat(h)
    }

    private fun validateIdentifiant(h: Hangar) {
        if (h.identifiant.isBlank())
            throw IllegalArgumentException("L'identifiant du hangar ne peut pas être vide.")

        if (!identifiantRegex.matches(h.identifiant.uppercase()))
            throw IllegalArgumentException(
                "Identifiant hangar '${h.identifiant}' invalide (ex: H1, H12, A3, B04)."
            )
    }

    private fun validateCapacity(h: Hangar) {
        if (h.capacite <= 0)
            throw IllegalArgumentException("La capacité du hangar doit être > 0.")
    }

    private fun validateEtat(h: Hangar) {
        if (h.etat !in HangarEtat.entries.toTypedArray()) {
            throw IllegalArgumentException(
                "État du hangar invalide : ${h.etat}"
            )
        }
    }
}
