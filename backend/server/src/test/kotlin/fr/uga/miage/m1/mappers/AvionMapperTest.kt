package fr.uga.miage.m1.mappers

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import fr.uga.miage.m1.models.Avion
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AvionMapperTest {
    @Test
    fun `toResponse maps fields`() {
        val a = Avion(immatriculation="F-MAP", type="A320", capacite=180, etat=AvionEtat.EN_SERVICE)
        val dto = a.toResponse()
        assertEquals("F-MAP", dto.immatriculation)
        assertEquals(180, dto.capacite)
    }
}
