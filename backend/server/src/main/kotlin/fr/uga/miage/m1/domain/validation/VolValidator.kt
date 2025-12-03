package fr.uga.miage.m1.domain.validation

import fr.uga.miage.m1.domain.model.Vol

object VolValidator {

    private val airportCodeRegex = Regex("^[A-Z]{3}\$") // Le code IATA

    // Valide un vol en vérifiant les aéroports et les dates
    fun validate(vol: Vol) {
        validateAirports(vol)
        validateDates(vol)
    }

    private fun validateAirports(vol: Vol) {
        //On doit tout d'abord vérifier que les codes aéroportuaires sont valides (format IATA)
        if (!airportCodeRegex.matches(vol.origine))
            throw IllegalArgumentException("Origine '${vol.origine}' invalide (format IATA requis).")
        //On doit tout d'abord vérifier que les codes aéroportuaires sont valides (format IATA)
        if (!airportCodeRegex.matches(vol.destination))
            throw IllegalArgumentException("Destination '${vol.destination}' invalide (format IATA requis).")
        //On doit vérifier que l'origine et la destination sont différentes
        if (vol.origine == vol.destination)
            throw IllegalArgumentException("Origine et destination doivent être différentes.")
    }

    private fun validateDates(vol: Vol) {
        //On doit vérifier que la date/heure de départ est avant celle d'arrivée, car c'est impossible d'arriver avant de partir :)
        if (vol.heureDepart.isAfter(vol.heureArrivee))
            throw IllegalArgumentException("La date/heure de départ doit être avant celle d'arrivée.")
    }
}