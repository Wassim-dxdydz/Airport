package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat

interface VolStatusStrategy {
    fun canTransition(from: VolEtat, to: VolEtat): Boolean
}

object DefaultVolStatusStrategy : VolStatusStrategy {
    private val allowedTransitions = mapOf(
        VolEtat.PREVU to setOf(VolEtat.EN_ATTENTE, VolEtat.ANNULE),
        VolEtat.EN_ATTENTE to setOf(VolEtat.EMBARQUEMENT, VolEtat.ANNULE),
        VolEtat.EMBARQUEMENT to setOf(VolEtat.DECOLLE, VolEtat.ANNULE),
        VolEtat.DECOLLE to setOf(VolEtat.EN_VOL),
        VolEtat.EN_VOL to setOf(VolEtat.ARRIVE, VolEtat.ANNULE),
        VolEtat.ARRIVE to emptySet(),
        VolEtat.ANNULE to emptySet()
    )

    override fun canTransition(from: VolEtat, to: VolEtat): Boolean =
        allowedTransitions[from]?.contains(to) ?: false
}
