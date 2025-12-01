package fr.uga.miage.m1.persistence.entity

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class PisteEntityTest {

    @Test
    fun `should create PisteEntity`() {
        val id = UUID.randomUUID()

        val entity = PisteEntity(
            id = id,
            identifiant = "R1",
            longueurM = 3200,
            etat = PisteEtat.LIBRE
        )

        assertEquals(id, entity.id)
        assertEquals("R1", entity.identifiant)
        assertEquals(3200, entity.longueurM)
        assertEquals(PisteEtat.LIBRE, entity.etat)
    }
}
