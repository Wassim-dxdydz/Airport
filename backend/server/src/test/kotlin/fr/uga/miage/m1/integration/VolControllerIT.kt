package fr.uga.miage.m1.integration

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.time.LocalDateTime
import java.util.UUID

class VolControllerIT : BaseIntegration() {

    @Test
    fun `POST then GET vol`() {
        val now = LocalDateTime.now()

        // create vol
        client.post().uri("/api/vols")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "numeroVol": "AF-IT01",
                  "origine": "CDG",
                  "destination": "JFK",
                  "heureDepart": "${now.plusHours(2)}",
                  "heureArrivee": "${now.plusHours(8)}"
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").value<String> { it.isNotBlank() }
            .jsonPath("$.numeroVol").isEqualTo("AF-IT01")
            .jsonPath("$.etat").isEqualTo(VolEtat.PREVU.name)

        // list
        client.get().uri("/api/vols")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].numeroVol").isEqualTo("AF-IT01")
            .jsonPath("$[0].etat").isEqualTo(VolEtat.PREVU.name)
    }

    @Test
    fun `assign then unassign avion`() {
        val now = LocalDateTime.now()

        // create avion
        client.post().uri("/api/avions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"immatriculation":"F-VOL-A1","type":"A320","capacite":180,"etat":"EN_SERVICE"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").value<String> { it.isNotBlank() }

        // create vol
        client.post().uri("/api/vols")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "numeroVol": "AF-ASSIGN",
                  "origine": "LYS",
                  "destination": "LHR",
                  "heureDepart": "${now.plusHours(1)}",
                  "heureArrivee": "${now.plusHours(3)}"
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").value<String> { it.isNotBlank() }

        // fetch first ids
        lateinit var volId: String
        lateinit var avionId: String

        client.get().uri("/api/vols").exchange().expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").value<String> { volId = it }

        client.get().uri("/api/avions").exchange().expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").value<String> { avionId = it }

        // assign
        client.post().uri("/api/vols/$volId/assign-avion/$avionId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.avionId").isEqualTo(avionId)

        // unassign
        client.post().uri("/api/vols/$volId/unassign-avion")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.avionId").doesNotExist()
    }

    @Test
    fun `update etat then filter by etat`() {
        val now = LocalDateTime.now()

        // create vol
        var volId: String? = null
        client.post().uri("/api/vols")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                {
                  "numeroVol": "AF-STATE",
                  "origine": "MRS",
                  "destination": "FCO",
                  "heureDepart": "${now.plusHours(2)}",
                  "heureArrivee": "${now.plusHours(4)}"
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").value<String> { volId = it }
            .jsonPath("$.etat").isEqualTo(VolEtat.PREVU.name)

        // patch state -> EN_VOL (endpoint expects a JSON string literal)
        client.patch().uri("/api/vols/$volId/etat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("\"EN_VOL\"")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.etat").isEqualTo(VolEtat.EN_VOL.name)

        // get by id to confirm
        client.get().uri("/api/vols/$volId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.etat").isEqualTo(VolEtat.EN_VOL.name)

        // filter by etat
        client.get().uri("/api/vols/etat/EN_VOL")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].etat").isEqualTo(VolEtat.EN_VOL.name)
    }
}
