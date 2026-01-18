package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat

object AvionStateFactory {
    fun fromEtat(etat: AvionEtat): AvionState =
        when (etat) {
            AvionEtat.EN_VOL -> EnVolState()
            AvionEtat.MAINTENANCE -> MaintenanceState()
            AvionEtat.DISPONIBLE -> DisponibleState()
        }
}
