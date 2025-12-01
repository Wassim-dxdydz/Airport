package fr.uga.miage.m1.domain.model

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class AvionTest {

    @Test
    fun `should create Avion with given properties`() {
        val id = UUID.randomUUID()
        val hangarId = UUID.randomUUID()

        val avion = Avion(
            id = id,
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = hangarId
        )

        assertEquals(id, avion.id)
        assertEquals("F-GRNB", avion.immatriculation)
        assertEquals("A320", avion.type)
        assertEquals(180, avion.capacite)
        assertEquals(AvionEtat.EN_SERVICE, avion.etat)
        assertEquals(hangarId, avion.hangarId)
    }

    @Test
    fun `should allow Avion without hangar`() {
        val avion = Avion(
            id = null,
            immatriculation = "F-TEST",
            type = "B737",
            capacite = 150,
            etat = AvionEtat.EN_SERVICE,
            hangarId = null
        )

        assertNull(avion.id)
        assertNull(avion.hangarId)
    }
}
