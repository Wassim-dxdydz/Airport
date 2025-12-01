package fr.uga.miage.m1.domain.service

import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.HangarDataPort
import fr.uga.miage.m1.exceptions.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.UUID

@Service
class AvionService(
    private val avionPort: AvionDataPort,
    private val hangarPort: HangarDataPort
) {

    // On liste tous les avions
    fun list(): Flux<Avion> =
        avionPort.findAll()

    // On récupère un avion par son id, sinon on lève une exception de type NotFoundException
    fun get(id: UUID): Mono<Avion> =
        avionPort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Avion $id not found")))

    // Créer un avion seulement si le hangar existe (business rule)
    fun create(avion: Avion): Mono<Avion> =
        checkHangarExists(avion.hangarId)
            .flatMap { avionPort.save(avion) }

    // On modifie l'avion (business rule: seulement si le hangar existe)
    fun update(id: UUID, avion: Avion): Mono<Avion> =
        get(id)
            .flatMap { current ->
                checkHangarExists(avion.hangarId)
                    .flatMap {
                        avionPort.save(
                            current.copy(
                                type = avion.type,
                                capacite = avion.capacite,
                                etat = avion.etat,
                                hangarId = avion.hangarId
                            )
                        )
                    }
            }

    // On supprime un avion par son id
    fun delete(id: UUID): Mono<Void> =
        avionPort.deleteById(id)

    // On attache un hangar à l'avion
    fun assignHangar(id: UUID, hangarId: UUID): Mono<Avion> =
        checkHangarExists(hangarId)
            .flatMap { get(id) }
            .flatMap { avion -> avionPort.save(avion.copy(hangarId = hangarId)) }

    // On détache le hangar de l'avion
    fun unassignHangar(id: UUID): Mono<Avion> =
        get(id)
            .flatMap { avionPort.save(it.copy(hangarId = null)) }

    // On vérifie si le hangar, sinon on lève une exception de type NotFoundException
    private fun checkHangarExists(hangarId: UUID?): Mono<Boolean> =
        if (hangarId == null) {
            Mono.just(true)
        } else {
            hangarPort.existsById(hangarId).flatMap { exists ->
                if (exists) Mono.just(true)
                else Mono.error(NotFoundException("Hangar $hangarId not found"))
            }
        }
}
