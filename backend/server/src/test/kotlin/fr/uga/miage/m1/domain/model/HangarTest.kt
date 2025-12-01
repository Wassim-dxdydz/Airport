package fr.uga.miage.m1.domain.model

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class HangarTest {

    @Test
    fun `should create Hangar with given properties`() {
        val id = UUID.randomUUID()

        val hangar = Hangar(
            id = id,
            identifiant = "H1",
            capacite = 10,
            etat = HangarEtat.DISPONIBLE
        )

        assertEquals(id, hangar.id)
        assertEquals("H1", hangar.identifiant)
        assertEquals(10, hangar.capacite)
        assertEquals(HangarEtat.DISPONIBLE, hangar.etat)
    }

    @Test
    fun `should allow Hangar without id`() {
        val hangar = Hangar(
            id = null,
            identifiant = "H2",
            capacite = 5,
            etat = HangarEtat.DISPONIBLE
        )

        assertNull(hangar.id)
    }
}
