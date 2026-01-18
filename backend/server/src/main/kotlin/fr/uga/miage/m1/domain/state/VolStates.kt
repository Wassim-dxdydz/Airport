package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat

abstract class AbstractVolState : VolState {
    override fun requiresAvion(): Boolean = true

    override fun requiresPiste(): Boolean = false

    override fun isPisteOccupied(): Boolean = false

    override fun isFinalState(): Boolean = false

    override fun canTransitionTo(newEtat: VolEtat): Boolean = false

    override fun transitionTo(newEtat: VolEtat): VolState {
        if (!canTransitionTo(newEtat)) {
            throw IllegalStateException(
                "Transition invalide de ${getEtat()} vers $newEtat"
            )
        }
        return VolStateFactory.fromEtat(newEtat)
    }
}

class PrevuVolState : AbstractVolState() {
    override fun getEtat(): VolEtat = VolEtat.PREVU

    override fun requiresAvion(): Boolean = true

    override fun canTransitionTo(newEtat: VolEtat): Boolean =
        newEtat in setOf(VolEtat.EN_ATTENTE, VolEtat.ANNULE)
}

class EnAttenteVolState : AbstractVolState() {
    override fun getEtat(): VolEtat = VolEtat.EN_ATTENTE

    override fun requiresAvion(): Boolean = true

    override fun canTransitionTo(newEtat: VolEtat): Boolean =
        newEtat in setOf(VolEtat.EMBARQUEMENT, VolEtat.ANNULE)
}

class EmbarquementVolState : AbstractVolState() {
    override fun getEtat(): VolEtat = VolEtat.EMBARQUEMENT

    override fun requiresAvion(): Boolean = true

    override fun canTransitionTo(newEtat: VolEtat): Boolean =
        newEtat in setOf(VolEtat.DECOLLE, VolEtat.ANNULE)
}

class DecolleVolState : AbstractVolState() {
    override fun getEtat(): VolEtat = VolEtat.DECOLLE

    override fun requiresAvion(): Boolean = true

    override fun requiresPiste(): Boolean = true

    override fun isPisteOccupied(): Boolean = true

    override fun canTransitionTo(newEtat: VolEtat): Boolean =
        newEtat == VolEtat.EN_VOL
}

class EnVolVolState : AbstractVolState() {
    override fun getEtat(): VolEtat = VolEtat.EN_VOL

    override fun requiresAvion(): Boolean = true

    override fun canTransitionTo(newEtat: VolEtat): Boolean =
        newEtat == VolEtat.ARRIVE
}

class ArriveVolState : AbstractVolState() {
    override fun getEtat(): VolEtat = VolEtat.ARRIVE

    override fun requiresAvion(): Boolean = true

    override fun requiresPiste(): Boolean = true

    override fun isPisteOccupied(): Boolean = true

    override fun canTransitionTo(newEtat: VolEtat): Boolean =
        newEtat == VolEtat.TERMINE
}

class TermineVolState : AbstractVolState() {
    override fun getEtat(): VolEtat = VolEtat.TERMINE

    override fun requiresAvion(): Boolean = false

    override fun isFinalState(): Boolean = true

    override fun canTransitionTo(newEtat: VolEtat): Boolean = false
}

class AnnuleVolState : AbstractVolState() {
    override fun getEtat(): VolEtat = VolEtat.ANNULE

    override fun requiresAvion(): Boolean = false

    override fun isFinalState(): Boolean = true

    override fun canTransitionTo(newEtat: VolEtat): Boolean = false
}
