package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat

interface AvionState {
    fun getEtat(): AvionEtat

    fun canAssignHangar(): Boolean = false

    fun transitionTo(newEtat: AvionEtat, hasActiveFlights: Boolean = false): AvionState
}
