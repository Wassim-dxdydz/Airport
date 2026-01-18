package fr.uga.miage.m1.domain.validation

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Vol
import java.time.Duration
import java.time.LocalDateTime

object VolValidator {

    private val airportCodeRegex = Regex("^[A-Z]{3}$")
    private val numeroVolRegex = Regex("^[A-Z]{2}[0-9]{1,4}$")

    private const val MIN_ADVANCE_BOOKING_HOURS = 2L
    private const val MIN_FLIGHT_DURATION_MINUTES = 2L

    fun validate(vol: Vol) {
        validateNumeroVol(vol)
        validateAirports(vol)
        validateDates(vol)
        validateEtat(vol)
        validateAvionAssignment(vol)
        validatePisteAssignment(vol)
    }

    fun validateForUpdate(vol: Vol) {
        validateNumeroVol(vol)
        validateAirports(vol)
        validateDatesForUpdate(vol)
    }

    private fun validateNumeroVol(vol: Vol) {
        if (vol.numeroVol.isBlank())
            throw IllegalArgumentException("Le numéro de vol ne peut pas être vide.")

        if (!numeroVolRegex.matches(vol.numeroVol.uppercase()))
            throw IllegalArgumentException(
                "Format de numéro de vol invalide : '${vol.numeroVol}'. " +
                        "Exemples valides : AF1234, BA456, LH23"
            )
    }

    private fun validateAirports(vol: Vol) {
        if (!airportCodeRegex.matches(vol.origine.uppercase()))
            throw IllegalArgumentException(
                "Origine '${vol.origine}' invalide. " +
                        "Le code aéroport doit être au format IATA (3 lettres). " +
                        "Exemples : CDG, ATL, JFK"
            )

        if (!airportCodeRegex.matches(vol.destination.uppercase()))
            throw IllegalArgumentException(
                "Destination '${vol.destination}' invalide. " +
                        "Le code aéroport doit être au format IATA (3 lettres). " +
                        "Exemples : CDG, ATL, JFK"
            )

        if (vol.origine.equals(vol.destination, ignoreCase = true))
            throw IllegalArgumentException(
                "Origine et destination doivent être différentes."
            )
    }

    private fun validateDates(vol: Vol) {
        val now = LocalDateTime.now()

        if (vol.heureDepart.isBefore(now.plusHours(MIN_ADVANCE_BOOKING_HOURS))) {
            throw IllegalArgumentException(
                "L'heure de départ doit être au moins ${MIN_ADVANCE_BOOKING_HOURS}h dans le futur. " +
                        "Départ prévu : ${vol.heureDepart}, Minimum requis : ${now.plusHours(MIN_ADVANCE_BOOKING_HOURS)}"
            )
        }

        if (!vol.heureDepart.isBefore(vol.heureArrivee)) {
            throw IllegalArgumentException(
                "L'heure de départ doit être avant l'heure d'arrivée. " +
                        "Départ : ${vol.heureDepart}, Arrivée : ${vol.heureArrivee}"
            )
        }

        val duration = Duration.between(vol.heureDepart, vol.heureArrivee)
        if (duration.toMinutes() < MIN_FLIGHT_DURATION_MINUTES) {
            throw IllegalArgumentException(
                "La durée du vol doit être d'au moins ${MIN_FLIGHT_DURATION_MINUTES} minutes. " +
                        "Durée actuelle : ${duration.toMinutes()} minutes"
            )
        }
    }

    private fun validateDatesForUpdate(vol: Vol) {
        if (!vol.heureDepart.isBefore(vol.heureArrivee)) {
            throw IllegalArgumentException(
                "L'heure de départ doit être avant l'heure d'arrivée. " +
                        "Départ : ${vol.heureDepart}, Arrivée : ${vol.heureArrivee}"
            )
        }

        val duration = Duration.between(vol.heureDepart, vol.heureArrivee)
        if (duration.toMinutes() < MIN_FLIGHT_DURATION_MINUTES) {
            throw IllegalArgumentException(
                "La durée du vol doit être d'au moins ${MIN_FLIGHT_DURATION_MINUTES} minutes. " +
                        "Durée actuelle : ${duration.toMinutes()} minutes"
            )
        }
    }

    private fun validateEtat(vol: Vol) {
        if (vol.etat != VolEtat.PREVU) {
            throw IllegalArgumentException(
                "L'état initial d'un vol doit être PREVU. " +
                        "État fourni : ${vol.etat}"
            )
        }
    }

    private fun validateAvionAssignment(vol: Vol) {
        if (vol.avionId == null) {
            throw IllegalArgumentException(
                "Un avion doit être assigné lors de la création du vol."
            )
        }
    }

    private fun validatePisteAssignment(vol: Vol) {
        if (vol.pisteId != null) {
            throw IllegalArgumentException(
                "La piste ne doit pas être assignée manuellement. " +
                        "Elle sera automatiquement attribuée lors du décollage/atterrissage."
            )
        }
    }
}
