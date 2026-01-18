package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat

abstract class AbstractAvionState : AvionState {
    override fun canAssignHangar(): Boolean = false

    override fun transitionTo(newEtat: AvionEtat, hasActiveFlights: Boolean): AvionState {
        if (newEtat == getEtat()) return this
        throw IllegalStateException("Transition invalide de ${getEtat()} vers $newEtat")
    }
}

class EnVolState : AbstractAvionState() {
    override fun getEtat(): AvionEtat = AvionEtat.EN_VOL
    override fun canAssignHangar(): Boolean = true

    override fun transitionTo(newEtat: AvionEtat, hasActiveFlights: Boolean): AvionState =
        when (newEtat) {
            AvionEtat.MAINTENANCE -> {
                check(!hasActiveFlights) { "Impossible de mettre l'avion en maintenance s'il a des vols actifs ou programmés" }
                MaintenanceState()
            }
            AvionEtat.DISPONIBLE -> DisponibleState()
            AvionEtat.EN_VOL -> this
        }
}

class MaintenanceState : AbstractAvionState() {
    override fun getEtat(): AvionEtat = AvionEtat.MAINTENANCE
    override fun canAssignHangar(): Boolean = true

    override fun transitionTo(newEtat: AvionEtat, hasActiveFlights: Boolean): AvionState =
        when (newEtat) {
            AvionEtat.EN_VOL -> EnVolState()
            AvionEtat.DISPONIBLE -> DisponibleState()
            AvionEtat.MAINTENANCE -> this
        }
}

class DisponibleState : AbstractAvionState() {
    override fun getEtat(): AvionEtat = AvionEtat.DISPONIBLE

    override fun transitionTo(newEtat: AvionEtat, hasActiveFlights: Boolean): AvionState =
        when (newEtat) {
            AvionEtat.MAINTENANCE -> {
                check(!hasActiveFlights) { "Impossible de mettre l'avion en maintenance s'il a des vols actifs ou programmés" }
                MaintenanceState()
            }
            AvionEtat.EN_VOL -> {
                check(!hasActiveFlights) { "Impossible de remettre l'avion en service s'il a des vols actifs ou programmés" }
                EnVolState()
            }
            AvionEtat.DISPONIBLE -> this
        }
}
