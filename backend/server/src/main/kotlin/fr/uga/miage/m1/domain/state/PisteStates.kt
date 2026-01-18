package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat

abstract class AbstractPisteState : PisteState {
    override fun canBeUsedForFlight(): Boolean = false

    override fun transitionTo(newEtat: PisteEtat): PisteState {
        if (newEtat == getEtat()) return this
        throw IllegalStateException("Transition invalide de ${getEtat()} vers $newEtat")
    }
}

class LibrePisteState : AbstractPisteState() {
    override fun getEtat(): PisteEtat = PisteEtat.LIBRE

    override fun canBeUsedForFlight(): Boolean = true

    override fun transitionTo(newEtat: PisteEtat): PisteState =
        when (newEtat) {
            PisteEtat.OCCUPEE -> OccupeePisteState()
            PisteEtat.MAINTENANCE -> MaintenancePisteState()
            PisteEtat.LIBRE -> this
        }
}

class OccupeePisteState : AbstractPisteState() {
    override fun getEtat(): PisteEtat = PisteEtat.OCCUPEE

    override fun transitionTo(newEtat: PisteEtat): PisteState =
        when (newEtat) {
            PisteEtat.LIBRE -> LibrePisteState()
            PisteEtat.MAINTENANCE -> {
                throw IllegalStateException(
                    "Impossible de mettre une piste en maintenance pendant qu'elle est occupée"
                )
            }
            PisteEtat.OCCUPEE -> this
        }
}

class MaintenancePisteState : AbstractPisteState() {
    override fun getEtat(): PisteEtat = PisteEtat.MAINTENANCE

    override fun transitionTo(newEtat: PisteEtat): PisteState =
        when (newEtat) {
            PisteEtat.LIBRE -> LibrePisteState()
            PisteEtat.OCCUPEE -> {
                throw IllegalStateException(
                    "Une piste en maintenance doit d'abord être remise en service (LIBRE)"
                )
            }
            PisteEtat.MAINTENANCE -> this
        }
}
