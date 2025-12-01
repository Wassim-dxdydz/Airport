package fr.uga.miage.m1.domain.service

import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.domain.port.HangarDataPort
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.exceptions.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class HangarService(
    private val hangarPort: HangarDataPort,
    private val avionPort: AvionDataPort
) {

    fun list(): Flux<Hangar> =
        hangarPort.findAll()

    fun get(id: UUID): Mono<Hangar> =
        hangarPort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Hangar $id not found")))

    fun create(hangar: Hangar): Mono<Hangar> =
        hangarPort.save(hangar)

    fun update(id: UUID, updated: Hangar): Mono<Hangar> =
        get(id).flatMap { current ->
            hangarPort.save(
                current.copy(
                    capacite = updated.capacite,
                    etat = updated.etat
                )
            )
        }

    fun delete(id: UUID): Mono<Void> =
        hangarPort.deleteById(id)

    fun listAvions(id: UUID) =
        avionPort.findAll()
            .filter { it.hangarId == id }
}
