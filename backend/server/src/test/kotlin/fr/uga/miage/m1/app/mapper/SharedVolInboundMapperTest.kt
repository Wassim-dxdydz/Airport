package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.requests.remote.SharedVolRequest
import fr.uga.miage.m1.responses.remote.SharedVolResponse
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class SharedVolInboundMapperTest {

    @Test
    fun toDomain_shouldMapCorrectly() {
        val now = LocalDateTime.now()
        val req = SharedVolRequest(
            numeroVol = "AF100",
            origine = "CDG",
            destination = "ALG",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionImmatriculation = "A320-XYZ",
            avionType = "A320",
            avionCapacite = 180,
            avionEtat = AvionEtat.EN_SERVICE
        )

        val (avion, vol) = SharedVolInboundMapper.toDomain(req)

        assertEquals("A320-XYZ", avion.immatriculation)
        assertEquals("A320", avion.type)
        assertEquals(180, avion.capacite)
        assertEquals(AvionEtat.EN_SERVICE, avion.etat)

        assertEquals("AF100", vol.numeroVol)
        assertEquals("CDG", vol.origine)
        assertEquals("ALG", vol.destination)
        assertEquals(now, vol.heureDepart)
        assertEquals(now.plusHours(2), vol.heureArrivee)
        assertEquals(VolEtat.PREVU, vol.etat)
    }

    @Test
    fun fromResponse_shouldMapCorrectly() {
        val now = LocalDateTime.now()
        val res = SharedVolResponse(
            numeroVol = "AF200",
            origine = "ALG",
            destination = "LYS",
            heureDepart = now,
            heureArrivee = now.plusHours(3),
            etat = VolEtat.EN_VOL,
            avionImmatriculation = "B737-123",
            avionType = "B737",
            avionCapacite = 150,
            avionEtat = AvionEtat.EN_SERVICE
        )

        val (avion, vol) = SharedVolInboundMapper.fromResponse(res)

        assertEquals("B737-123", avion.immatriculation)
        assertEquals("B737", avion.type)
        assertEquals(150, avion.capacite)
        assertEquals(AvionEtat.EN_SERVICE, avion.etat)

        assertEquals("AF200", vol.numeroVol)
        assertEquals("ALG", vol.origine)
        assertEquals("LYS", vol.destination)
        assertEquals(now, vol.heureDepart)
        assertEquals(now.plusHours(3), vol.heureArrivee)
        assertEquals(VolEtat.EN_VOL, vol.etat)
    }
}
