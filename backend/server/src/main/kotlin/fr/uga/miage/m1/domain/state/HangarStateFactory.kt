package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat

object HangarStateFactory {
    fun fromEtat(etat: HangarEtat): HangarState =
        when (etat) {
            HangarEtat.DISPONIBLE -> DisponibleHangarState()
            HangarEtat.PLEIN -> PleinHangarState()
            HangarEtat.MAINTENANCE -> MaintenanceHangarState()
        }
}
