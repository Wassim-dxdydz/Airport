package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.requests.UpdateVolRequest
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

    @Test
    fun `toDomain maps CreateVolRequest to Vol domain`() {
        val now = LocalDateTime.now()

        val req = CreateVolRequest(
            numeroVol = "AF999",
            origine = "LYS",
            destination = "NCE",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(3)
        )

        val result = VolMapper.toDomain(req)

        assertEquals("AF999", result.numeroVol)
        assertEquals("LYS", result.origine)
        assertEquals("NCE", result.destination)
        assertEquals(req.heureDepart, result.heureDepart)
        assertEquals(req.heureArrivee, result.heureArrivee)
        assertEquals(VolEtat.PREVU, result.etat)
        assertEquals(null, result.avionId)
        assertEquals(null, result.id)
    }

    @Test
    fun `toUpdatedDomain merges non-null fields and preserves numeroVol`() {
        val now = LocalDateTime.now()

        val current = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF123",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null
        )

        val req = UpdateVolRequest(
            origine = "LYS",
            destination = "NCE",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(4),
            etat = VolEtat.DECOLLE,
            avionId = UUID.randomUUID()
        )

        val updated = VolMapper.toUpdatedDomain(current, req)

        assertEquals("AF123", updated.numeroVol)

        assertEquals("LYS", updated.origine)
        assertEquals("NCE", updated.destination)
        assertEquals(now.plusHours(1), updated.heureDepart)
        assertEquals(now.plusHours(4), updated.heureArrivee)
        assertEquals(VolEtat.DECOLLE, updated.etat)
        assertEquals(req.avionId, updated.avionId)
    }
}
