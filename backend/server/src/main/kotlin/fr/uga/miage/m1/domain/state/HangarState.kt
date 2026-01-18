package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat

interface HangarState {
    fun getEtat(): HangarEtat

    fun canAcceptAvion(): Boolean = false

    fun transitionTo(
        newEtat: HangarEtat,
        currentOccupancy: Int = 0,
        capacity: Int = 0
    ): HangarState
}
