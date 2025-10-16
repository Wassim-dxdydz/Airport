package fr.uga.miage.m1.integration

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class PisteControllerIT : BaseIntegration() {

    @Test
    fun `create piste, set etat, list disponibles`() {
        // create
        client.post().uri("/api/pistes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"identifiant":"R1","longueurM":3200,"etat":"LIBRE"}""")
            .exchange()
            .expectStatus().isCreated

        // set etat
        val pisteId = client.get().uri("/api/pistes").exchange().expectStatus().isOk
            .expectBody().jsonPath("$[0].id").value<String> { }

        client.put().uri("/api/pistes/$pisteId/etat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"etat":"OCCUPEE"}""")
            .exchange()
            .expectStatus().isOk

        // disponibles should be empty now
        client.get().uri("/api/pistes/disponibles")
            .exchange()
            .expectStatus().isOk
            .expectBody().json("[]")
    }
}
