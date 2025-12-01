package fr.uga.miage.m1.domain.model

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class PisteTest {

    @Test
    fun `should create Piste with given properties`() {
        val id = UUID.randomUUID()

        val piste = Piste(
            id = id,
            identifiant = "P1",
            longueurM = 3000,
            etat = PisteEtat.LIBRE
        )

        assertEquals(id, piste.id)
        assertEquals("P1", piste.identifiant)
        assertEquals(3000, piste.longueurM)
        assertEquals(PisteEtat.LIBRE, piste.etat)
    }

    @Test
    fun `should allow Piste without id`() {
        val piste = Piste(
            id = null,
            identifiant = "P2",
            longueurM = 2500,
            etat = PisteEtat.OCCUPEE
        )

        assertNull(piste.id)
    }
}
