package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.domain.model.Hangar
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
}
