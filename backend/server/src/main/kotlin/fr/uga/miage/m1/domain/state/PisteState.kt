package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat

interface PisteState {
    fun getEtat(): PisteEtat

    fun canBeUsedForFlight(): Boolean = false

    fun transitionTo(newEtat: PisteEtat): PisteState
}
