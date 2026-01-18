package fr.uga.miage.m1.app.mapper

import fr.uga.miage.m1.domain.model.Passenger
import fr.uga.miage.m1.requests.CreatePassengerRequest
import fr.uga.miage.m1.requests.UpdatePassengerRequest
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PassengerMapperTest {

    @Test
    fun `toDomain maps CreatePassengerRequest to Passenger domain`() {
        val req = CreatePassengerRequest(
            prenom = "  Jean  ",
            nom = "  Dupont  ",
            email = "  Jean.Dupont@EXAMPLE.COM  ",
            telephone = "  +33612345678  "
        )

        val result = PassengerMapper.toDomain(req)

        assertEquals("Jean", result.prenom)
        assertEquals("Dupont", result.nom)
        assertEquals("jean.dupont@example.com", result.email)
        assertEquals("+33612345678", result.telephone)
        assertNull(result.id)
    }

    @Test
    fun `toDomain handles null telephone`() {
        val req = CreatePassengerRequest(
            prenom = "Sophie",
            nom = "Martin",
            email = "sophie.martin@example.com",
            telephone = null
        )

        val result = PassengerMapper.toDomain(req)

        assertEquals("Sophie", result.prenom)
        assertEquals("Martin", result.nom)
        assertEquals("sophie.martin@example.com", result.email)
        assertNull(result.telephone)
        assertNull(result.id)
    }

    @Test
    fun `toUpdatedDomain merges non-null fields`() {
        val current = Passenger(
            id = UUID.randomUUID(),
            prenom = "Jean",
            nom = "Dupont",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )

        val req = UpdatePassengerRequest(
            prenom = "  Jean-Pierre  ",
            nom = null,
            email = null,
            telephone = "  +33699999999  "
        )

        val updated = PassengerMapper.toUpdatedDomain(current, req)

        assertEquals("Jean-Pierre", updated.prenom)
        assertEquals("Dupont", updated.nom)
        assertEquals("jean.dupont@example.com", updated.email)
        assertEquals("+33699999999", updated.telephone)
        assertEquals(current.id, updated.id)
    }

    @Test
    fun `toUpdatedDomain keeps current values when all fields null`() {
        val current = Passenger(
            id = UUID.randomUUID(),
            prenom = "Sophie",
            nom = "Martin",
            email = "sophie.martin@example.com",
            telephone = "+33687654321"
        )

        val req = UpdatePassengerRequest(
            prenom = null,
            nom = null,
            email = null,
            telephone = null
        )

        val updated = PassengerMapper.toUpdatedDomain(current, req)

        assertEquals("Sophie", updated.prenom)
        assertEquals("Martin", updated.nom)
        assertEquals("sophie.martin@example.com", updated.email)
        assertEquals("+33687654321", updated.telephone)
        assertEquals(current.id, updated.id)
    }

    @Test
    fun `toUpdatedDomain trims and lowercases email when provided`() {
        val current = Passenger(
            id = UUID.randomUUID(),
            prenom = "Pierre",
            nom = "Lefebvre",
            email = "old@example.com",
            telephone = "+33698765432"
        )

        val req = UpdatePassengerRequest(
            prenom = null,
            nom = null,
            email = "  NEW.EMAIL@EXAMPLE.COM  ",
            telephone = null
        )

        val updated = PassengerMapper.toUpdatedDomain(current, req)

        assertEquals("new.email@example.com", updated.email)
        assertEquals("Pierre", updated.prenom)
        assertEquals("Lefebvre", updated.nom)
    }

    @Test
    fun `toResponse maps Passenger domain to PassengerResponse`() {
        val passenger = Passenger(
            id = UUID.randomUUID(),
            prenom = "Jean",
            nom = "Dupont",
            email = "jean.dupont@example.com",
            telephone = "+33612345678"
        )

        val resp = PassengerMapper.toResponse(passenger)

        assertEquals(passenger.id, resp.id)
        assertEquals("Jean", resp.prenom)
        assertEquals("Dupont", resp.nom)
        assertEquals("jean.dupont@example.com", resp.email)
        assertEquals("+33612345678", resp.telephone)
    }

    @Test
    fun `toResponse handles null telephone`() {
        val passenger = Passenger(
            id = UUID.randomUUID(),
            prenom = "Marie",
            nom = "Dubois",
            email = "marie.dubois@example.com",
            telephone = null
        )

        val resp = PassengerMapper.toResponse(passenger)

        assertEquals(passenger.id, resp.id)
        assertEquals("Marie", resp.prenom)
        assertEquals("Dubois", resp.nom)
        assertEquals("marie.dubois@example.com", resp.email)
        assertNull(resp.telephone)
    }
}
