package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.requests.CreateHangarRequest
import fr.uga.miage.m1.requests.UpdateHangarRequest
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class HangarMapperTest {

    @Test
    fun `toResponse maps domain to DTO`() {
        val h = Hangar(
            id = UUID.randomUUID(),
            identifiant = "H1",
            capacite = 10,
            etat = HangarEtat.DISPONIBLE
        )

        val dto = HangarMapper.toResponse(h)

        assertEquals("H1", dto.identifiant)
        assertEquals(10, dto.capacite)
        assertEquals(HangarEtat.DISPONIBLE, dto.etat)
    }

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
        // ID is always null on creation
        kotlin.test.assertNull(result.id)
    }

    @Test
    fun `toUpdatedDomain merges fields correctly`() {
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


}
