package fr.uga.miage.m1.domain.validation

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.domain.model.Avion

object AvionValidator {

    // Immatriculations françaises, européennes, internationales, FAA (USA)
    private val immatRegex = Regex("^([A-Z]{1,2}-[A-Z0-9]{3,5}|N[0-9A-Z]{1,5})$")

    // Type d'avion: Boeing (B737, B747), Airbus (A320, A380), Embraer (E190), etc.
    private val typeRegex = Regex("^[A-Z]{1,3}[0-9]{2,4}[A-Z]?(-[0-9]{1,3}[A-Z]{0,3})?$")

    fun validate(avion: Avion) {
        validateImmatriculation(avion)
        validateType(avion)
        validateCapacite(avion)
        validateEtat(avion)
        validateHangarAssignment(avion)
    }

    private fun validateImmatriculation(avion: Avion) {
        if (avion.immatriculation.isBlank())
            throw IllegalArgumentException("L'immatriculation ne peut pas être vide.")

        if (!immatRegex.matches(avion.immatriculation.uppercase()))
            throw IllegalArgumentException(
                "Format d'immatriculation invalide : '${avion.immatriculation}'. " +
                        "Exemples valides : F-BZHE, HB-JCA, D-ABCD, N12345"
            )
    }

    private fun validateType(avion: Avion) {
        if (avion.type.isBlank())
            throw IllegalArgumentException("Le type d'avion ne peut pas être vide.")

        if (!typeRegex.matches(avion.type.uppercase()))
            throw IllegalArgumentException(
                "Format de type d'avion invalide : '${avion.type}'. " +
                        "Exemples valides : A320, B737, A380-800, B777-300ER, E190"
            )
    }

    private fun validateCapacite(avion: Avion) {
        if (avion.capacite <= 0)
            throw IllegalArgumentException("La capacité doit être un entier > 0.")
    }

    private fun validateEtat(avion: Avion) {
        if (avion.etat !in setOf(AvionEtat.DISPONIBLE, AvionEtat.MAINTENANCE)) {
            throw IllegalArgumentException(
                "L'état de l'avion doit être DISPONIBLE ou MAINTENANCE. " +
                        "État actuel : ${avion.etat}"
            )
        }
    }

    private fun validateHangarAssignment(avion: Avion) {
        when (avion.etat) {
            AvionEtat.DISPONIBLE -> {
                if (avion.hangarId == null) {
                    throw IllegalArgumentException(
                        "Un avion DISPONIBLE doit être assigné à un hangar."
                    )
                }
            }
            AvionEtat.MAINTENANCE -> {
            }
            AvionEtat.EN_VOL -> {
                throw IllegalArgumentException(
                    "L'état EN_VOL ne peut être défini que via les transitions d'état."
                )
            }
        }
    }

}
