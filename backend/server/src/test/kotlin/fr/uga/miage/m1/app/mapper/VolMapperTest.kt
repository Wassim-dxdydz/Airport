package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.requests.CreateVolRequest
import fr.uga.miage.m1.requests.UpdateVolRequest
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VolMapperTest {

    @Test
    fun `toDomain maps CreateVolRequest to Vol domain with PREVU state`() {
        val now = LocalDateTime.now()
        val avionId = UUID.randomUUID()

        val req = CreateVolRequest(
            numeroVol = "AF999",
            origine = "LYS",
            destination = "NCE",
            heureDepart = now.plusHours(1),
            heureArrivee = now.plusHours(3),
            avionId = avionId
        )

        val result = VolMapper.toDomain(req)

        assertEquals("AF999", result.numeroVol)
        assertEquals("LYS", result.origine)
        assertEquals("NCE", result.destination)
        assertEquals(req.heureDepart, result.heureDepart)
        assertEquals(req.heureArrivee, result.heureArrivee)
        assertEquals(VolEtat.PREVU, result.etat) // Always PREVU on creation
        assertEquals(avionId, result.avionId)
        assertNull(result.pisteId) // Always null on creation
        assertNull(result.id)
    }


    @Test
    fun `toPatchedDomain keeps current values when all fields null`() {
        val now = LocalDateTime.now()

        val current = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF456",
            origine = "ORY",
            destination = "TLS",
            heureDepart = now,
            heureArrivee = now.plusHours(1),
            etat = VolEtat.EN_VOL,
            avionId = UUID.randomUUID(),
            pisteId = null
        )

        val req = UpdateVolRequest(
            origine = null,
            destination = null,
            heureDepart = null,
            heureArrivee = null,
            etat = null
        )

        val updated = VolMapper.toPatchedDomain(current, req)

        assertEquals(current.id, updated.id)
        assertEquals("AF456", updated.numeroVol)
        assertEquals("ORY", updated.origine)
        assertEquals("TLS", updated.destination)
        assertEquals(current.heureDepart, updated.heureDepart)
        assertEquals(current.heureArrivee, updated.heureArrivee)
        assertEquals(VolEtat.EN_VOL, updated.etat)
        assertEquals(current.avionId, updated.avionId)
        assertNull(updated.pisteId)
    }

    @Test
    fun `toPatchedDomain updates etat when provided`() {
        val now = LocalDateTime.now()

        val current = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF789",
            origine = "CDG",
            destination = "BCN",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = UUID.randomUUID(),
            pisteId = null
        )

        val req = UpdateVolRequest(
            origine = null,
            destination = null,
            heureDepart = null,
            heureArrivee = null,
            etat = VolEtat.EMBARQUEMENT
        )

        val updated = VolMapper.toPatchedDomain(current, req)

        assertEquals(VolEtat.EMBARQUEMENT, updated.etat)
        assertEquals(current.origine, updated.origine)
        assertEquals(current.destination, updated.destination)
    }

    @Test
    fun `toPatchedDomain preserves numeroVol avionId and pisteId`() {
        val now = LocalDateTime.now()
        val avionId = UUID.randomUUID()
        val pisteId = UUID.randomUUID()

        val current = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF-ORIGINAL",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = avionId,
            pisteId = pisteId
        )

        val req = UpdateVolRequest(
            origine = "LYS",
            destination = "BCN",
            heureDepart = now.plusHours(3),
            heureArrivee = now.plusHours(5),
            etat = VolEtat.EN_ATTENTE
        )

        val updated = VolMapper.toPatchedDomain(current, req)

        // These should NEVER change via patch
        assertEquals("AF-ORIGINAL", updated.numeroVol)
        assertEquals(avionId, updated.avionId)
        assertEquals(pisteId, updated.pisteId)
        // These should update
        assertEquals("LYS", updated.origine)
        assertEquals("BCN", updated.destination)
        assertEquals(VolEtat.EN_ATTENTE, updated.etat)
    }

    @Test
    fun `toResponse maps Vol domain to VolResponse`() {
        val now = LocalDateTime.now()
        val avionId = UUID.randomUUID()
        val pisteId = UUID.randomUUID()

        val v = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF123",
            origine = "CDG",
            destination = "MAD",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = avionId,
            pisteId = pisteId
        )

        val dto = VolMapper.toResponse(v)

        assertEquals(v.id, dto.id)
        assertEquals("AF123", dto.numeroVol)
        assertEquals("CDG", dto.origine)
        assertEquals("MAD", dto.destination)
        assertEquals(now, dto.heureDepart)
        assertEquals(now.plusHours(2), dto.heureArrivee)
        assertEquals(VolEtat.PREVU, dto.etat)
        assertEquals(avionId, dto.avionId)
        assertEquals(pisteId, dto.pisteId)
        assertNull(dto.createdAt)
        assertNull(dto.updatedAt)
    }

    @Test
    fun `toResponse handles null avionId and pisteId`() {
        val now = LocalDateTime.now()

        val v = Vol(
            id = UUID.randomUUID(),
            numeroVol = "AF456",
            origine = "ORY",
            destination = "LYS",
            heureDepart = now,
            heureArrivee = now.plusHours(1),
            etat = VolEtat.ANNULE,
            avionId = null,
            pisteId = null
        )

        val dto = VolMapper.toResponse(v)

        assertEquals(v.id, dto.id)
        assertEquals("AF456", dto.numeroVol)
        assertEquals(VolEtat.ANNULE, dto.etat)
        assertNull(dto.avionId)
        assertNull(dto.pisteId)
    }
}
