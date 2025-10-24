package fr.uga.miage.m1.mappers

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.models.Vol
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VolMapperTest {

    @Test
    fun `toResponse maps fields`() {
        val now = LocalDateTime.now().plusHours(2)
        val later = now.plusHours(6)

        val vol = Vol(
            numeroVol = "AF123",
            origine = "CDG",
            destination = "JFK",
            heureDepart = now,
            heureArrivee = later,
            etat = VolEtat.PREVU
        )

        val dto = vol.toResponse()

        assertEquals("AF123", dto.numeroVol)
        assertEquals("CDG", dto.origine)
        assertEquals("JFK", dto.destination)
        assertEquals(now, dto.heureDepart)
        assertEquals(later, dto.heureArrivee)
        assertEquals(VolEtat.PREVU, dto.etat)

        assertNull(dto.createdAt)
        assertNull(dto.updatedAt)
    }
}
