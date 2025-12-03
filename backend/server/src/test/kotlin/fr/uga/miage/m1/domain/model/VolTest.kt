package fr.uga.miage.m1.domain.model

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class VolTest {

    @Test
    fun `should create Vol with given properties`() {
        val id = UUID.randomUUID()
        val avionId = UUID.randomUUID()
        val now = LocalDateTime.now()
        val later = now.plusHours(2)

        val vol = Vol(
            id = id,
            numeroVol = "AF123",
            origine = "CDG",
            destination = "LHR",
            heureDepart = now,
            heureArrivee = later,
            etat = VolEtat.PREVU,
            avionId = avionId,
            pisteId = null
        )

        assertEquals(id, vol.id)
        assertEquals("AF123", vol.numeroVol)
        assertEquals("CDG", vol.origine)
        assertEquals("LHR", vol.destination)
        assertEquals(now, vol.heureDepart)
        assertEquals(later, vol.heureArrivee)
        assertEquals(VolEtat.PREVU, vol.etat)
        assertEquals(avionId, vol.avionId)
    }

    @Test
    fun `should allow Vol without avion`() {
        val now = LocalDateTime.now()
        val later = now.plusHours(1)

        val vol = Vol(
            id = null,
            numeroVol = "AF999",
            origine = "ORY",
            destination = "NCE",
            heureDepart = now,
            heureArrivee = later,
            etat = VolEtat.EN_ATTENTE,
            avionId = null,
            pisteId = null
        )

        assertNull(vol.id)
        assertNull(vol.avionId)
    }
}
