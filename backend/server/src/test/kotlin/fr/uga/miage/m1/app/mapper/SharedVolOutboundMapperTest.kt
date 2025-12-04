package fr.uga.miage.m1.app.mapper

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.model.Vol
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class SharedVolOutboundMapperTest {

    @Test
    fun toResponse_shouldMapCorrectly() {
        val now = LocalDateTime.now()

        val avion = Avion(
            id = null,
            immatriculation = "A320-999",
            type = "A320",
            capacite = 180,
            etat = AvionEtat.EN_SERVICE,
            hangarId = null
        )

        val vol = Vol(
            id = null,
            numeroVol = "AF100",
            origine = "CDG",
            destination = "ALG",
            heureDepart = now,
            heureArrivee = now.plusHours(2),
            etat = VolEtat.PREVU,
            avionId = null,
            pisteId = null
        )

        val res = SharedVolOutboundMapper.toResponse(vol, avion)

        assertEquals("AF100", res.numeroVol)
        assertEquals("CDG", res.origine)
        assertEquals("ALG", res.destination)
        assertEquals(now, res.heureDepart)
        assertEquals(now.plusHours(2), res.heureArrivee)
        assertEquals(VolEtat.PREVU, res.etat)

        assertEquals("A320-999", res.avionImmatriculation)
        assertEquals("A320", res.avionType)
        assertEquals(180, res.avionCapacite)
        assertEquals(AvionEtat.EN_SERVICE, res.avionEtat)
    }

    @Test
    fun toRequest_shouldMapCorrectly() {
        val now = LocalDateTime.now()

        val avion = Avion(
            id = null,
            immatriculation = "B737-123",
            type = "B737",
            capacite = 150,
            etat = AvionEtat.EN_SERVICE,
            hangarId = null
        )

        val vol = Vol(
            id = null,
            numeroVol = "AF200",
            origine = "ALG",
            destination = "LYS",
            heureDepart = now,
            heureArrivee = now.plusHours(3),
            etat = VolEtat.EN_VOL,
            avionId = null,
            pisteId = null
        )

        val req = SharedVolOutboundMapper.toRequest(vol, avion)

        assertEquals("AF200", req.numeroVol)
        assertEquals("ALG", req.origine)
        assertEquals("LYS", req.destination)
        assertEquals(now, req.heureDepart)
        assertEquals(now.plusHours(3), req.heureArrivee)
        assertEquals(VolEtat.EN_VOL, req.etat)

        assertEquals("B737-123", req.avionImmatriculation)
        assertEquals("B737", req.avionType)
        assertEquals(150, req.avionCapacite)
        assertEquals(AvionEtat.EN_SERVICE, req.avionEtat)
    }
}
