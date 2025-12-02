package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.requests.CreateAvionRequest
import fr.uga.miage.m1.requests.UpdateAvionRequest
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AvionMapperTest {

    @Test
    fun `toDomain maps CreateAvionRequest to Avion domain`() {
        val req = CreateAvionRequest(
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = null
        )

        val result = AvionMapper.toDomain(req)

        assertEquals("F-GRNB", result.immatriculation)
        assertEquals("A320", result.type)
        assertEquals(180, result.capacite)
        assertEquals(AvionEtat.EN_SERVICE, result.etat)
        assertNull(result.hangarId)
        assertNull(result.id)
    }

    @Test
    fun `toUpdatedDomain merges non-null fields`() {
        val current = Avion(
            id = UUID.randomUUID(),
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = null
        )

        val req = UpdateAvionRequest(
            type = "A321",
            capacite = null,
            etat = null,
            hangarId = null
        )

        val updated = AvionMapper.toUpdatedDomain(current, req)

        assertEquals("A321", updated.type)
        assertEquals(180, updated.capacite)
        assertEquals(AvionEtat.EN_SERVICE, updated.etat)
    }

    @Test
    fun `toResponse maps Avion domain to AvionResponse`() {
        val avion = Avion(
            id = UUID.randomUUID(),
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = UUID.randomUUID()
        )

        val resp = AvionMapper.toResponse(avion)

        assertEquals(avion.id, resp.id)
        assertEquals("F-GRNB", resp.immatriculation)
        assertEquals("A320", resp.type)
        assertEquals(180, resp.capacite)
        assertEquals(AvionEtat.EN_SERVICE, resp.etat)
        assertEquals(avion.hangarId, resp.hangarId)
    }

}
