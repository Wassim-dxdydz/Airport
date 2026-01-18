package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat

object PisteStateFactory {
    fun fromEtat(etat: PisteEtat): PisteState =
        when (etat) {
            PisteEtat.LIBRE -> LibrePisteState()
            PisteEtat.OCCUPEE -> OccupeePisteState()
            PisteEtat.MAINTENANCE -> MaintenancePisteState()
        }
}
