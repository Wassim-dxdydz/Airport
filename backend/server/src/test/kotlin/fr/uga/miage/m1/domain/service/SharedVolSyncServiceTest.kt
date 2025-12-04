package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.RemoteAirportPort
import fr.uga.miage.m1.domain.port.VolDataPort
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.UUID

@TestPropertySource(properties = [
    "local.airport.code=ATL",
    "remote.airport.code=CDG"
])
class SharedVolSyncServiceTest {

    private lateinit var avionPort: AvionDataPort
    private lateinit var volPort: VolDataPort
    private lateinit var remotePort: RemoteAirportPort
    private lateinit var service: SharedVolSyncService

    @BeforeEach
    fun setup() {
        avionPort = mockk()
        volPort = mockk()
        remotePort = mockk()

        service = SharedVolSyncService(
            avionPort,
            volPort,
            remotePort,
            myAirportCode = "ATL",
            partnerAirportCode = "CDG"
        )
    }

    @Test
    fun `import creates new avion and new vol when none exist`() {
        val avion = Avion(null, "A320-ABC", "A320", 180, AvionEtat.EN_SERVICE, null)
        val vol = Vol(
            null, "AF100", "ATL", "CDG",
            LocalDateTime.now(), LocalDateTime.now().plusHours(2),
            VolEtat.PREVU, null, null
        )

        val savedAvion = avion.copy(id = UUID.randomUUID())
        val savedVol = vol.copy(id = UUID.randomUUID(), avionId = savedAvion.id)

        every { avionPort.findByImmatriculation("A320-ABC") } returns Mono.empty()
        every { avionPort.save(avion) } returns Mono.just(savedAvion)

        every { volPort.findByNumeroVol("AF100") } returns Mono.empty()
        every { volPort.save(any()) } returns Mono.just(savedVol)

        StepVerifier.create(service.import(avion, vol))
            .expectNext(savedVol)
            .verifyComplete()
    }

    @Test
    fun `exportForPartner returns vol-avion pairs`() {
        val vol = Vol(
            UUID.randomUUID(), "AF300", "ATL", "CDG",
            LocalDateTime.now(), LocalDateTime.now().plusHours(2),
            VolEtat.PREVU, UUID.randomUUID(), null
        )

        val avion = Avion(vol.avionId, "A320-XYZ", "A320", 180, AvionEtat.EN_SERVICE, null)

        every { volPort.findByDestination("CDG") } returns Flux.just(vol)
        every { avionPort.findById(vol.avionId!!) } returns Mono.just(avion)

        StepVerifier.create(service.exportForPartner())
            .expectNext(vol to avion)
            .verifyComplete()
    }
}
