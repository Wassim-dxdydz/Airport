package fr.uga.miage.m1.persistence.entity

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class AvionEntityTest {

    @Test
    fun `should create AvionEntity`() {
        val id = UUID.randomUUID()
        val hangarId = UUID.randomUUID()

        val entity = AvionEntity(
            id = id,
            immatriculation = "F-GRNB",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = hangarId
        )

        assertEquals(id, entity.id)
        assertEquals("F-GRNB", entity.immatriculation)
        assertEquals("A320", entity.type)
        assertEquals(180, entity.capacite)
        assertEquals(AvionEtat.EN_SERVICE, entity.etat)
        assertEquals(hangarId, entity.hangarId)
    }
}
