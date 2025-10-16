package fr.uga.miage.m1.mappers

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.models.Piste
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PisteMapperTest {

    @Test
    fun `toResponse maps fields`() {
        val piste = Piste(
            identifiant = "R1",
            longueurM = 3500,
            etat = PisteEtat.LIBRE
        )

        val dto = piste.toResponse()

        assertEquals("R1", dto.identifiant)
        assertEquals(3500, dto.longueurM)
        assertEquals(PisteEtat.LIBRE, dto.etat)
    }
}
