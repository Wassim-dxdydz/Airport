package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultVolStatusStrategyTest {

    private val strategy = DefaultVolStatusStrategy

    @Test
    fun `PREVU transitions are correct`() {
        assertTrue(strategy.canTransition(VolEtat.PREVU, VolEtat.EN_ATTENTE))
        assertTrue(strategy.canTransition(VolEtat.PREVU, VolEtat.ANNULE))

        assertFalse(strategy.canTransition(VolEtat.PREVU, VolEtat.EMBARQUEMENT))
        assertFalse(strategy.canTransition(VolEtat.PREVU, VolEtat.DECOLLE))
        assertFalse(strategy.canTransition(VolEtat.PREVU, VolEtat.ARRIVE))
    }

    @Test
    fun `EN_ATTENTE transitions are correct`() {
        assertTrue(strategy.canTransition(VolEtat.EN_ATTENTE, VolEtat.EMBARQUEMENT))
        assertTrue(strategy.canTransition(VolEtat.EN_ATTENTE, VolEtat.ANNULE))

        assertFalse(strategy.canTransition(VolEtat.EN_ATTENTE, VolEtat.EN_VOL))
        assertFalse(strategy.canTransition(VolEtat.EN_ATTENTE, VolEtat.PREVU))
        assertFalse(strategy.canTransition(VolEtat.EN_ATTENTE, VolEtat.ARRIVE))
    }

    @Test
    fun `EMBARQUEMENT transitions are correct`() {
        assertTrue(strategy.canTransition(VolEtat.EMBARQUEMENT, VolEtat.DECOLLE))
        assertTrue(strategy.canTransition(VolEtat.EMBARQUEMENT, VolEtat.ANNULE))

        assertFalse(strategy.canTransition(VolEtat.EMBARQUEMENT, VolEtat.EN_VOL))
        assertFalse(strategy.canTransition(VolEtat.EMBARQUEMENT, VolEtat.PREVU))
    }

    @Test
    fun `DEC0LLE transitions are correct`() {
        assertTrue(strategy.canTransition(VolEtat.DECOLLE, VolEtat.EN_VOL))

        assertFalse(strategy.canTransition(VolEtat.DECOLLE, VolEtat.ARRIVE))
        assertFalse(strategy.canTransition(VolEtat.DECOLLE, VolEtat.ANNULE))
    }

    @Test
    fun `EN_VOL transitions are correct`() {
        assertTrue(strategy.canTransition(VolEtat.EN_VOL, VolEtat.ARRIVE))
        assertTrue(strategy.canTransition(VolEtat.EN_VOL, VolEtat.ANNULE))

        assertFalse(strategy.canTransition(VolEtat.EN_VOL, VolEtat.DECOLLE))
    }

    @Test
    fun `ARRIVE has no valid transitions`() {
        VolEtat.values().forEach { to ->
            if (to != VolEtat.ARRIVE) {
                assertFalse(strategy.canTransition(VolEtat.ARRIVE, to))
            }
        }
    }

    @Test
    fun `ANNULE has no valid transitions`() {
        VolEtat.values().forEach { to ->
            if (to != VolEtat.ANNULE) {
                assertFalse(strategy.canTransition(VolEtat.ANNULE, to))
            }
        }
    }
}
