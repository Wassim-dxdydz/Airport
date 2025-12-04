package fr.uga.miage.m1.app.controller

import fr.uga.miage.m1.app.mapper.VolHistoryMapper
import fr.uga.miage.m1.domain.model.VolHistory
import fr.uga.miage.m1.domain.port.VolHistoryDataPort
import fr.uga.miage.m1.responses.VolHistoryResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.time.LocalDateTime
import java.util.UUID

class VolHistoryControllerTest {

    private lateinit var client: WebTestClient
    private lateinit var port: VolHistoryDataPort

    @BeforeEach
    fun setup() {
        port = mockk()
        mockkObject(VolHistoryMapper)
        val controller = VolHistoryController(port)
        client = WebTestClient.bindToController(controller).build()
    }

    @Test
    fun getHistory_shouldReturnMappedHistory() {
        val volId = UUID.randomUUID()
        val domainItem = VolHistory(
            id = UUID.randomUUID(),
            volId = volId,
            etat = backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat.PREVU,
            changedAt = LocalDateTime.now()
        )
        val responseItem = VolHistoryResponse(
            id = domainItem.id,
            etat = domainItem.etat,
            changedAt = domainItem.changedAt
        )

        every { port.findByVolId(volId) } returns Flux.just(domainItem)
        every { VolHistoryMapper.toResponse(domainItem) } returns responseItem

        client.get()
            .uri("/api/vols/$volId/history")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(VolHistoryResponse::class.java)
            .contains(responseItem)

        verify { port.findByVolId(volId) }
        verify { VolHistoryMapper.toResponse(domainItem) }
    }
}
