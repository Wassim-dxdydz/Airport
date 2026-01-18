package fr.uga.miage.m1.app.mapper

import fr.uga.miage.m1.domain.model.CheckIn
import fr.uga.miage.m1.requests.CreateCheckInRequest
import fr.uga.miage.m1.requests.UpdateCheckInRequest
import fr.uga.miage.m1.responses.PassengerResponse
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CheckInMapperTest {

    @Test
    fun `toDomain maps CreateCheckInRequest to CheckIn domain`() {
        val req = CreateCheckInRequest(
            passagerId = UUID.randomUUID(),
            volId = UUID.randomUUID(),
            numeroSiege = "  12a  "
        )

        val result = CheckInMapper.toDomain(req)

        assertEquals(req.passagerId, result.passagerId)
        assertEquals(req.volId, result.volId)
        assertEquals("12A", result.numeroSiege)
        assertNotNull(result.heureCheckIn)
        assertNull(result.id)
    }

    @Test
    fun `toUpdatedDomain merges non-null numeroSiege`() {
        val current = CheckIn(
            id = UUID.randomUUID(),
            passagerId = UUID.randomUUID(),
            volId = UUID.randomUUID(),
            numeroSiege = "12A",
            heureCheckIn = LocalDateTime.now().minusHours(1)
        )

        val req = UpdateCheckInRequest(
            numeroSiege = "  15c  "
        )

        val updated = CheckInMapper.toUpdatedDomain(current, req)

        assertEquals("15C", updated.numeroSiege)
        assertEquals(current.id, updated.id)
        assertEquals(current.passagerId, updated.passagerId)
        assertEquals(current.volId, updated.volId)
        assertEquals(current.heureCheckIn, updated.heureCheckIn)
    }

    @Test
    fun `toUpdatedDomain keeps current numeroSiege when null`() {
        val current = CheckIn(
            id = UUID.randomUUID(),
            passagerId = UUID.randomUUID(),
            volId = UUID.randomUUID(),
            numeroSiege = "12A",
            heureCheckIn = LocalDateTime.now().minusHours(1)
        )

        val req = UpdateCheckInRequest(
            numeroSiege = null
        )

        val updated = CheckInMapper.toUpdatedDomain(current, req)

        assertEquals("12A", updated.numeroSiege)
        assertEquals(current.heureCheckIn, updated.heureCheckIn)
    }

    @Test
    fun `toResponse maps CheckIn domain to CheckInResponse without passagerInfo`() {
        val checkIn = CheckIn(
            id = UUID.randomUUID(),
            passagerId = UUID.randomUUID(),
            volId = UUID.randomUUID(),
            numeroSiege = "12A",
            heureCheckIn = LocalDateTime.now()
        )

        val resp = CheckInMapper.toResponse(checkIn)

        assertEquals(checkIn.id, resp.id)
        assertEquals(checkIn.passagerId, resp.passagerId)
        assertEquals(checkIn.volId, resp.volId)
        assertEquals("12A", resp.numeroSiege)
        assertEquals(checkIn.heureCheckIn, resp.heureCheckIn)
        assertNull(resp.passagerInfo)
    }

    @Test
    fun `toResponse maps CheckIn domain to CheckInResponse with passagerInfo`() {
        val checkIn = CheckIn(
            id = UUID.randomUUID(),
            passagerId = UUID.randomUUID(),
            volId = UUID.randomUUID(),
            numeroSiege = "12A",
            heureCheckIn = LocalDateTime.now()
        )

        val passagerInfo = PassengerResponse(
            id = checkIn.passagerId,
            nom = "Dupont",
            prenom = "Jean",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )

        val resp = CheckInMapper.toResponse(checkIn, passagerInfo)

        assertEquals(checkIn.id, resp.id)
        assertEquals("12A", resp.numeroSiege)
        assertNotNull(resp.passagerInfo)
        assertEquals("Dupont", resp.passagerInfo?.nom)
        assertEquals("Jean", resp.passagerInfo?.prenom)
    }
}
