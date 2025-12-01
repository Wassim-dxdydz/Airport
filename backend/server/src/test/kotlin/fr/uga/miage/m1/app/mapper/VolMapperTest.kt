package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Vol
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

class VolMapperTest {

    @Test
    fun `toResponse maps domain to DTO`() {
        val now = LocalDateTime.now()
        val v = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF123",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null
        )

        val dto = VolMapper.toResponse(v)

        assertEquals("AF123", dto.numeroVol)
        assertEquals("CDG", dto.origine)
        assertEquals("MAD", dto.destination)
        assertEquals(VolEtat.PREVU, dto.etat)
    }
}
