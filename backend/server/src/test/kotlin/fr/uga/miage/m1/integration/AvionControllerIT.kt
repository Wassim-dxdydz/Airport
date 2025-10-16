package fr.uga.miage.m1.integration

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.UUID

class AvionControllerIT : BaseIntegration() {

    @Test
    fun `POST then GET avion`() {
        val createdId =
            client.post().uri("/api/avions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""{
                  "immatriculation":"F-IT01","type":"A320","capacite":180,"etat":"EN_SERVICE"
                }""".trimIndent())
                .exchange()
                .expectStatus().isCreated
                .expectBody()
                .jsonPath("$.id").value<String> { it.isNotBlank() }
                .jsonPath("$.immatriculation").isEqualTo("F-IT01")
                .returnResult()
                .responseBody
                .let { String(it!!) }

        // list
        client.get().uri("/api/avions")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].etat").isEqualTo(AvionEtat.EN_SERVICE.name)
    }

    @Test
    fun `assign then unassign hangar`() {
        // create hangar
        val hangarId = client.post().uri("/api/hangars")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"identifiant":"H-1","capacite":5,"etat":"DISPONIBLE"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").value<String> { }

        // create avion
        val avionId = client.post().uri("/api/avions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"immatriculation":"F-ASSIGN","type":"A320","capacite":180,"etat":"EN_SERVICE"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").value<String> { }

        val ids = client.get().uri("/api/avions").exchange().expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").value<String> { }

        val firstAvionId = client.get().uri("/api/avions").exchange().expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").value<String> { }

        val firstHangarId = client.get().uri("/api/hangars").exchange().expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").value<String> { }

        client.post().uri("/api/avions/$firstAvionId/assign-hangar/$firstHangarId")
            .exchange()
            .expectStatus().isOk

        client.post().uri("/api/avions/$firstAvionId/unassign-hangar")
            .exchange()
            .expectStatus().isOk
    }
}
