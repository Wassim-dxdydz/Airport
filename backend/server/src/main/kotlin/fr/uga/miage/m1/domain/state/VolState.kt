package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat

interface VolState {
    fun getEtat(): VolEtat

    fun requiresAvion(): Boolean

    fun requiresPiste(): Boolean

    fun isPisteOccupied(): Boolean

    fun isFinalState(): Boolean = false

    fun canTransitionTo(newEtat: VolEtat): Boolean

    fun transitionTo(newEtat: VolEtat): VolState
}
