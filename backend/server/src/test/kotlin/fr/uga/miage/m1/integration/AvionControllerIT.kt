package fr.uga.miage.m1.integration

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.AvionEtat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class AvionControllerIT : BaseIntegration() {

    @Test
    fun `POST then GET avion`() {
        // Create avion
        client.post().uri("/api/avions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{
                "immatriculation":"F-IT01",
                "type":"A320",
                "capacite":180,
                "etat":"EN_SERVICE"
            }""".trimIndent())
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.immatriculation").isEqualTo("F-IT01")
            .jsonPath("$.etat").isEqualTo("EN_SERVICE")

        // List avions
        client.get().uri("/api/avions")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isNumber
            .jsonPath("$[0].etat").isEqualTo("EN_SERVICE")
    }

    @Test
    fun `assign then unassign hangar`() {
        // Create hangar first
        client.post().uri("/api/hangars")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{
                "identifiant":"H-1",
                "capacite":5,
                "etat":"DISPONIBLE"
            }""".trimIndent())
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").value<String> { hangarId ->
                // Create avion
                client.post().uri("/api/avions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""{
                        "immatriculation":"F-ASSIGN",
                        "type":"A320", 
                        "capacite":180,
                        "etat":"EN_SERVICE"
                    }""".trimIndent())
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody()
                    .jsonPath("$.id").value<String> { avionId ->
                        // Assign hangar
                        client.post().uri("/api/avions/$avionId/assign-hangar/$hangarId")
                            .exchange()
                            .expectStatus().isOk

                        // Unassign hangar
                        client.post().uri("/api/avions/$avionId/unassign-hangar")
                            .exchange()
                            .expectStatus().isOk
                    }
            }
    }
}