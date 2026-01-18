package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.domain.model.Piste
import fr.uga.miage.m1.requests.CreatePisteRequest
import fr.uga.miage.m1.requests.UpdatePisteRequest
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class PisteMapperTest {

    @Test
    fun `toResponse maps domain to DTO`() {
        val p = Piste(
            id = UUID.randomUUID(),
            identifiant = "R1",
            longueurM = 3200,
            etat = PisteEtat.LIBRE
        )

        val dto = PisteMapper.toResponse(p)

        assertEquals("R1", dto.identifiant)
        assertEquals(3200, dto.longueurM)
        assertEquals(PisteEtat.LIBRE, dto.etat)
    }

    @Test
    fun `toDomain maps CreatePisteRequest to Piste domain`() {
        val req = CreatePisteRequest(
            identifiant = "R1",
            longueurM = 3200,
            etat = PisteEtat.LIBRE
        )

        val result = PisteMapper.toDomain(req)

        assertEquals("R1", result.identifiant)
        assertEquals(3200, result.longueurM)
        assertEquals(PisteEtat.LIBRE, result.etat)
        kotlin.test.assertNull(result.id)
    }

    @Test
    fun `toUpdatedDomain updates only the etat field`() {
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
}
