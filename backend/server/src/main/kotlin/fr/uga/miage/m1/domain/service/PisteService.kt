package fr.uga.miage.m1.domain.service

import fr.uga.miage.m1.domain.model.Piste
import fr.uga.miage.m1.domain.port.PisteDataPort
import fr.uga.miage.m1.exceptions.NotFoundException
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.UUID

@Service
class PisteService(
    private val pistePort: PisteDataPort
) {

    fun list(): Flux<Piste> =
        pistePort.findAll()

    fun get(id: UUID): Mono<Piste> =
        pistePort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Piste $id not found")))

    fun create(piste: Piste): Mono<Piste> =
        pistePort.save(piste)

    fun updateEtat(id: UUID, newEtat: PisteEtat): Mono<Piste> =
        get(id).flatMap { existing ->
            pistePort.save(
                existing.copy(etat = newEtat)
            )
        }

    fun delete(id: UUID): Mono<Void> =
        pistePort.deleteById(id)

    fun disponibles(): Flux<Piste> =
        pistePort.findByEtat(PisteEtat.LIBRE)
}
