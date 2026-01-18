package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.domain.model.Piste
import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteRequest
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PisteMapperTest {

    @Test
    fun `toDomain maps CreatePisteRequest to Piste domain with LIBRE state`() {
        val req = CreatePisteRequest(
            identifiant = "R1",
            longueurM = 3200,
            etat = PisteEtat.OCCUPEE // This should be ignored
        )

        val result = PisteMapper.toDomain(req)

        assertEquals("R1", result.identifiant)
        assertEquals(3200, result.longueurM)
        assertEquals(PisteEtat.LIBRE, result.etat) // Always LIBRE on creation
        assertNull(result.id)
    }

    @Test
    fun `toUpdatedDomain merges non-null fields`() {
        val existing = Piste(
            id = UUID.randomUUID(),
            identifiant = "R1",
            longueurM = 3200,
            etat = PisteEtat.LIBRE
        )

        val req = UpdatePisteRequest(
            identifiant = null,
            longueurM = null,
            etat = PisteEtat.OCCUPEE
        )

        val updated = PisteMapper.toUpdatedDomain(existing, req)

        assertEquals(existing.id, updated.id)
        assertEquals("R1", updated.identifiant)
        assertEquals(3200, updated.longueurM)
        assertEquals(PisteEtat.OCCUPEE, updated.etat)
    }

    @Test
    fun `toUpdatedDomain keeps current values when all fields null`() {
        val existing = Piste(
            id = UUID.randomUUID(),
            identifiant = "R2",
            longueurM = 2500,
            etat = PisteEtat.MAINTENANCE
        )

        val req = UpdatePisteRequest(
            identifiant = null,
            longueurM = null,
            etat = null
        )

        val updated = PisteMapper.toUpdatedDomain(existing, req)

        assertEquals(existing.id, updated.id)
        assertEquals("R2", updated.identifiant)
        assertEquals(2500, updated.longueurM)
        assertEquals(PisteEtat.MAINTENANCE, updated.etat)
    }

    @Test
    fun `toUpdatedDomain updates all fields when provided`() {
        val existing = Piste(
            id = UUID.randomUUID(),
            identifiant = "R1",
            longueurM = 3200,
            etat = PisteEtat.LIBRE
        )

        val req = UpdatePisteRequest(
            identifiant = "R1-BIS",
            longueurM = 3500,
            etat = PisteEtat.OCCUPEE
        )

        val updated = PisteMapper.toUpdatedDomain(existing, req)

        assertEquals(existing.id, updated.id)
        assertEquals("R1-BIS", updated.identifiant)
        assertEquals(3500, updated.longueurM)
        assertEquals(PisteEtat.OCCUPEE, updated.etat)
    }

    @Test
    fun `toResponse maps Piste domain to PisteResponse`() {
        val p = Piste(
            id = UUID.randomUUID(),
            identifiant = "R1",
            longueurM = 3200,
            etat = PisteEtat.LIBRE
        )

        val dto = PisteMapper.toResponse(p)

        assertEquals(p.id, dto.id)
        assertEquals("R1", dto.identifiant)
        assertEquals(3200, dto.longueurM)
        assertEquals(PisteEtat.LIBRE, dto.etat)
    }
}
