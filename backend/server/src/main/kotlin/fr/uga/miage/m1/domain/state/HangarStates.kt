package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat

abstract class AbstractHangarState : HangarState {

    override fun canAcceptAvion(): Boolean = false

    override fun transitionTo(
        newEtat: HangarEtat,
        currentOccupancy: Int,
        capacity: Int
    ): HangarState {
        if (newEtat == getEtat()) return this
        throw IllegalStateException("Transition invalide de ${getEtat()} vers $newEtat")
    }
}

class DisponibleHangarState : AbstractHangarState() {

    override fun getEtat(): HangarEtat = HangarEtat.DISPONIBLE

    override fun canAcceptAvion(): Boolean = true

    override fun transitionTo(
        newEtat: HangarEtat,
        currentOccupancy: Int,
        capacity: Int
    ): HangarState =
        when (newEtat) {
            HangarEtat.PLEIN -> {
                check(currentOccupancy >= capacity) {
                    "Impossible de passer le hangar à PLEIN: occupancy ($currentOccupancy) < capacité ($capacity)"
                }
                PleinHangarState()
            }
            HangarEtat.MAINTENANCE -> {
                check(currentOccupancy == 0) {
                    "Impossible de mettre le hangar en maintenance s'il contient des avions ($currentOccupancy)"
                }
                MaintenanceHangarState()
            }
            HangarEtat.DISPONIBLE -> this
        }
}

class PleinHangarState : AbstractHangarState() {

    override fun getEtat(): HangarEtat = HangarEtat.PLEIN

    override fun transitionTo(
        newEtat: HangarEtat,
        currentOccupancy: Int,
        capacity: Int
    ): HangarState =
        when (newEtat) {
            HangarEtat.DISPONIBLE -> {
                check(currentOccupancy < capacity) {
                    "Impossible de passer le hangar à DISPONIBLE: occupancy ($currentOccupancy) >= capacité ($capacity)"
                }
                DisponibleHangarState()
            }
            HangarEtat.MAINTENANCE -> {
                check(currentOccupancy == 0) {
                    "Impossible de mettre le hangar en maintenance s'il contient des avions ($currentOccupancy)"
                }
                MaintenanceHangarState()
            }
            HangarEtat.PLEIN -> this
        }
}

class MaintenanceHangarState : AbstractHangarState() {

    override fun getEtat(): HangarEtat = HangarEtat.MAINTENANCE

    override fun transitionTo(
        newEtat: HangarEtat,
        currentOccupancy: Int,
        capacity: Int
    ): HangarState =
        when (newEtat) {
            HangarEtat.DISPONIBLE -> DisponibleHangarState()
            HangarEtat.PLEIN -> {
                check(currentOccupancy >= capacity) {
                    "Impossible de passer le hangar à PLEIN: occupancy ($currentOccupancy) < capacité ($capacity)"
                }
                PleinHangarState()
            }
            HangarEtat.MAINTENANCE -> this
        }
}
