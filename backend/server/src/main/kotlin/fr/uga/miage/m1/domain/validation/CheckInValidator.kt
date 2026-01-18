package fr.uga.miage.m1.domain.validation

import fr.uga.miage.m1.domain.model.CheckIn

object CheckInValidator {

    private val VALID_COLUMNS = listOf('A', 'B', 'C', 'D', 'E', 'F')
    private const val COLUMNS_PER_ROW = 6

    fun validate(checkIn: CheckIn) {
        validateNumeroSiege(checkIn.numeroSiege)
    }

    fun validateNumeroSiege(numeroSiege: String) {
        if (numeroSiege.isBlank()) {
            throw IllegalArgumentException("Le numéro de siège ne peut pas être vide")
        }

        val pattern = Regex("^(\\d+)([A-Z])\$")
        val matchResult = pattern.matchEntire(numeroSiege.trim().uppercase())

        if (matchResult == null) {
            if (numeroSiege.matches(Regex("^[A-Z]\\d+\$"))) {
                throw IllegalArgumentException(
                    "Format de siège incorrect: \"$numeroSiege\". " +
                            "Le numéro doit être avant la lettre (ex: 12A au lieu de A12)"
                )
            }
            throw IllegalArgumentException(
                "Format de siège invalide: \"$numeroSiege\". " +
                        "Format attendu: [Numéro][Lettre] (ex: 12A, 5F)"
            )
        }

        val rowNumber = matchResult.groupValues[1].toIntOrNull()
        if (rowNumber == null || rowNumber < 1) {
            throw IllegalArgumentException("Le numéro de rangée doit être supérieur ou égal à 1")
        }

        val column = matchResult.groupValues[2][0]
        if (column !in VALID_COLUMNS) {
            val validColumnsStr = VALID_COLUMNS.joinToString(", ")
            throw IllegalArgumentException(
                "Colonne \"$column\" invalide. " +
                        "Les avions commerciaux ont 6 colonnes: $validColumnsStr"
            )
        }
    }

    fun validateSeatForCapacity(numeroSiege: String, capacite: Int): String? {
        if (capacite % COLUMNS_PER_ROW != 0) {
            return "Configuration de l'avion invalide (capacité doit être divisible par 6)"
        }

        val pattern = Regex("^(\\d+)([A-Z])\$")
        val matchResult = pattern.matchEntire(numeroSiege.trim().uppercase()) ?: return null

        val rowNumber = matchResult.groupValues[1].toIntOrNull() ?: return null
        val maxRows = capacite / COLUMNS_PER_ROW

        if (rowNumber > maxRows) {
            return "Rangée $rowNumber inexistante. " +
                    "Cet avion a $maxRows rangées " +
                    "(capacité: $capacite places = $maxRows rangées × 6 sièges)"
        }

        return null
    }
}
