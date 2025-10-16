package fr.uga.miage.m1.integration

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class HangarControllerIT : BaseIntegration() {

    @Test
    fun `create hangar, GET avions empty then with one avion`() {
        // create hangar
        client.post().uri("/api/hangars")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"identifiant":"H2","capacite":10,"etat":"DISPONIBLE"}""")
            .exchange()
            .expectStatus().isCreated

        val hangarId = client.get().uri("/api/hangars").exchange().expectStatus().isOk
            .expectBody().jsonPath("$[0].id").value<String> { }

        // list avions in hangar -> []
        client.get().uri("/api/hangars/$hangarId/avions")
            .exchange()
            .expectStatus().isOk
            .expectBody().json("[]")

        // create avion assigned to hangar
        client.post().uri("/api/avions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{
               "immatriculation":"F-H2",
               "type":"A320",
               "capacite":180,
               "etat":"EN_SERVICE",
               "hangarId":"$hangarId"
            }""")
            .exchange()
            .expectStatus().isCreated

        // list again -> not empty
        client.get().uri("/api/hangars/$hangarId/avions")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].immatriculation").isEqualTo("F-H2")
    }
}
