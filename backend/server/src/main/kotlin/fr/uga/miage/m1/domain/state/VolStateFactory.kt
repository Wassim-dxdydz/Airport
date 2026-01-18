package fr.uga.miage.m1.domain.state

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat

object VolStateFactory {
    fun fromEtat(etat: VolEtat): VolState =
        when (etat) {
            VolEtat.PREVU -> PrevuVolState()
            VolEtat.EN_ATTENTE -> EnAttenteVolState()
            VolEtat.EMBARQUEMENT -> EmbarquementVolState()
            VolEtat.DECOLLE -> DecolleVolState()
            VolEtat.EN_VOL -> EnVolVolState()
            VolEtat.ARRIVE -> ArriveVolState()
            VolEtat.TERMINE -> TermineVolState()
            VolEtat.ANNULE -> AnnuleVolState()
        }
}
