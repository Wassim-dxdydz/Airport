package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.SharedVolInboundMapper
import fr.uga.miage.m1.app.mapper.SharedVolOutboundMapper
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.service.SharedVolSyncService
import fr.uga.miage.m1.requests.remote.SharedVolRequest
import fr.uga.miage.m1.responses.remote.SharedVolResponse
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class RemoteSharedVolControllerTest {

    private lateinit var client: WebTestClient
    private lateinit var syncService: SharedVolSyncService

    @BeforeEach
    fun setup() {
        syncService = mockk()
        mockkObject(SharedVolInboundMapper)
        mockkObject(SharedVolOutboundMapper)

        val controller = RemoteSharedVolController(syncService)
        client = WebTestClient.bindToController(controller).build()
    }

    @Test
    fun importVol_shouldCallMapperAndService() {
        val req = SharedVolRequest(
            numeroVol = "AF123",
            origine = "CDG",
            destination = "ALG",
            heureDepart = LocalDateTime.now(),
            heureArrivee = LocalDateTime.now().plusHours(2),
            etat = backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat.PREVU,
            avionImmatriculation = "A320-999",
            avionType = "A320",
            avionCapacite = 180,
            avionEtat = backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat.EN_SERVICE
        )

        val avion = mockk<Avion>()
        val vol = mockk<Vol>()

        every { SharedVolInboundMapper.toDomain(req) } returns (avion to vol)
        every { syncService.import(avion, vol) } returns Mono.empty()

        client.post()
            .uri("/api/shared/vols/import")
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated

        verify { SharedVolInboundMapper.toDomain(req) }
        verify { syncService.import(avion, vol) }
    }

    @Test
    fun exportVols_shouldMapOutput() {
        val vol = mockk<Vol>()
        val avion = mockk<Avion>()

        val response = SharedVolResponse(
            numeroVol = "AF123",
            origine = "CDG",
            destination = "ALG",
            heureDepart = LocalDateTime.now(),
            heureArrivee = LocalDateTime.now().plusHours(2),
            etat = backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat.EN_VOL,
            avionImmatriculation = "A320-999",
            avionType = "A320",
            avionCapacite = 180,
            avionEtat = backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat.EN_SERVICE
        )

        every { syncService.exportForPartner() } returns Flux.just(vol to avion)
        every { SharedVolOutboundMapper.toResponse(vol, avion) } returns response

        client.get()
            .uri("/api/shared/vols/export")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(SharedVolResponse::class.java)
            .contains(response)

        verify { syncService.exportForPartner() }
        verify { SharedVolOutboundMapper.toResponse(vol, avion) }
    }
}
