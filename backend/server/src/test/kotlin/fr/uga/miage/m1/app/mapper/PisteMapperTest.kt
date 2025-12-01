package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.domain.model.Piste
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
}
