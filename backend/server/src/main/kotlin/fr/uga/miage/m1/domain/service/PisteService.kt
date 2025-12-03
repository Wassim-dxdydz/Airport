package fr.uga.miage.m1.domain.service

import fr.uga.miage.m1.domain.model.Piste
import fr.uga.miage.m1.domain.port.PisteDataPort
import fr.uga.miage.m1.exceptions.NotFoundException
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.domain.validation.PisteValidator
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.UUID

@Service
class PisteService(
    private val pistePort: PisteDataPort
) {
    // Liste toutes les pistes
    fun list(): Flux<Piste> =
        pistePort.findAll()

    // Récupère une piste par son id
    fun get(id: UUID): Mono<Piste> =
        pistePort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Piste $id not found")))

    // Créer une piste (avec validation)
    fun create(piste: Piste): Mono<Piste> {
        PisteValidator.validate(piste)
        return pistePort.save(piste)
    }

    // Met à jour l'état d'une piste
    fun updateEtat(id: UUID, newEtat: PisteEtat): Mono<Piste> =
        get(id).flatMap { existing ->
            val updated = existing.copy(etat = newEtat)
            PisteValidator.validate(updated)
            pistePort.save(updated)
        }

    // Supprime une piste
    fun delete(id: UUID): Mono<Unit> =
        get(id).flatMap { piste ->
            if (piste.etat == PisteEtat.OCCUPEE)
                return@flatMap Mono.error<Unit>(
                    IllegalStateException("Impossible de supprimer une piste encore occupée.")
                )

            pistePort.deleteById(id).thenReturn(Unit)
        }

    // Liste les pistes disponibles
    fun disponibles(): Flux<Piste> =
        pistePort.findByEtat(PisteEtat.LIBRE)
}
