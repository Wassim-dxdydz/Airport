package fr.uga.miage.m1.persistence.entity

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class VolEntityTest {

    @Test
    fun `should create VolEntity`() {
        val id = UUID.randomUUID()
        val avionId = UUID.randomUUID()
        val now = LocalDateTime.now()

        val entity = VolEntity(
            id = id,
            numeroVol = "AF1000",
            origine = "CDG",
            destination = "LHR",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = avionId,
            createdAt = now,
            updatedAt = now.plusMinutes(5)
        )

        assertEquals(id, entity.id)
        assertEquals("AF1000", entity.numeroVol)
        assertEquals("CDG", entity.origine)
        assertEquals(VolEtat.PREVU, entity.etat)
        assertEquals(avionId, entity.avionId)
    }
}
