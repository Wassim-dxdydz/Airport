package fr.uga.miage.m1.persistence.entity

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.HangarEtat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class HangarEntityTest {

    @Test
    fun `should create HangarEntity`() {
        val id = UUID.randomUUID()

        val entity = HangarEntity(
            id = id,
            identifiant = "H1",
            capacite = 5,
            etat = HangarEtat.DISPONIBLE
        )

        assertEquals(id, entity.id)
        assertEquals("H1", entity.identifiant)
        assertEquals(5, entity.capacite)
        assertEquals(HangarEtat.DISPONIBLE, entity.etat)
    }
}
