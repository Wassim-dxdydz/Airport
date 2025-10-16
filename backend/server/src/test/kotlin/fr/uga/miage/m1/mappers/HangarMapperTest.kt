package fr.uga.miage.m1.mappers

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import fr.uga.miage.m1.models.Hangar
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HangarMapperTest {

    @Test
    fun `toResponse maps fields`() {
        val hangar = Hangar(
            identifiant = "H1",
            capacite = 10,
            etat = HangarEtat.DISPONIBLE
        )

        val dto = hangar.toResponse()

        assertEquals("H1", dto.identifiant)
        assertEquals(10, dto.capacite)
        assertEquals(HangarEtat.DISPONIBLE, dto.etat)
    }
}
