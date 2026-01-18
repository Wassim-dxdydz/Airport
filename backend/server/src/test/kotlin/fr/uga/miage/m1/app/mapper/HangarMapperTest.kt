package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.requests.CreateHangarRequest
import fr.uga.miage.m1.requests.UpdateHangarRequest
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HangarMapperTest {

    @Test
    fun `toDomain maps CreateHangarRequest to Hangar domain`() {
        val req = CreateHangarRequest(
            identifiant = "H1",
            capacite = 10,
            etat = HangarEtat.DISPONIBLE
        )

        val result = HangarMapper.toDomain(req)

        assertEquals("H1", result.identifiant)
        assertEquals(10, result.capacite)
        assertEquals(HangarEtat.DISPONIBLE, result.etat)
        assertNull(result.id)
    }

    @Test
    fun `toUpdatedDomain merges non-null fields`() {
        val current = Hangar(
            id = UUID.randomUUID(),
            identifiant = "H1",
            capacite = 10,
            etat = HangarEtat.DISPONIBLE
        )

        val req = UpdateHangarRequest(
            capacite = 20,
            etat = null
        )

        val updated = HangarMapper.toUpdatedDomain(current, req)

        assertEquals(current.id, updated.id)
        assertEquals("H1", updated.identifiant)
        assertEquals(20, updated.capacite)
        assertEquals(HangarEtat.DISPONIBLE, updated.etat)
    }

    @Test
    fun `toUpdatedDomain keeps current values when all fields null`() {
        val current = Hangar(
            id = UUID.randomUUID(),
            identifiant = "H2",
            capacite = 15,
            etat = HangarEtat.MAINTENANCE
        )

        val req = UpdateHangarRequest(
            capacite = null,
            etat = null
        )

        val updated = HangarMapper.toUpdatedDomain(current, req)

        assertEquals(current.id, updated.id)
        assertEquals("H2", updated.identifiant)
        assertEquals(15, updated.capacite)
        assertEquals(HangarEtat.MAINTENANCE, updated.etat)
    }

    @Test
    fun `toUpdatedDomain preserves identifiant`() {
        val current = Hangar(
            id = UUID.randomUUID(),
            identifiant = "H-ORIGINAL",
            capacite = 10,
            etat = HangarEtat.DISPONIBLE
        )

        val req = UpdateHangarRequest(
            capacite = 25,
            etat = HangarEtat.PLEIN
        )

        val updated = HangarMapper.toUpdatedDomain(current, req)

        assertEquals("H-ORIGINAL", updated.identifiant)
        assertEquals(25, updated.capacite)
        assertEquals(HangarEtat.PLEIN, updated.etat)
    }

    @Test
    fun `toResponse maps Hangar domain to HangarResponse`() {
        val h = Hangar(
            id = UUID.randomUUID(),
            identifiant = "H1",
            capacite = 10,
            etat = HangarEtat.DISPONIBLE
        )

        val dto = HangarMapper.toResponse(h)

        assertEquals(h.id, dto.id)
        assertEquals("H1", dto.identifiant)
        assertEquals(10, dto.capacite)
        assertEquals(HangarEtat.DISPONIBLE, dto.etat)
    }
}
