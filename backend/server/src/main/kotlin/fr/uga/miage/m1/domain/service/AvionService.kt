package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import fr.uga.miage.m1.domain.model.Avion
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.HangarDataPort
import fr.uga.miage.m1.domain.port.VolDataPort
import fr.uga.miage.m1.domain.validation.AvionValidator
import fr.uga.miage.m1.exceptions.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.UUID

@Service
class AvionService(
    private val avionPort: AvionDataPort,
    private val hangarPort: HangarDataPort,
    private val volPort: VolDataPort
) {

    // Liste tous les avions
    fun list(): Flux<Avion> =
        avionPort.findAll()

    // Récupère un avion par son id
    fun get(id: UUID): Mono<Avion> =
        avionPort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Avion $id not found")))

    // Créer un avion (avec validation + vérification hangar)
    fun create(avion: Avion): Mono<Avion> {
        AvionValidator.validate(avion)

        return checkHangarExists(avion.hangarId)
            .flatMap {
                avionPort.save(avion)
            }
    }

    // Met à jour un avion
    fun update(id: UUID, avion: Avion): Mono<Avion> =
        get(id).flatMap { current ->

            val merged = current.copy(
                immatriculation = avion.immatriculation,
                type = avion.type,
                capacite = avion.capacite,
                etat = avion.etat,
                hangarId = avion.hangarId
            )

            AvionValidator.validate(merged) //On valide avant de sauvegarder
            checkHangarExists(merged.hangarId)
                .flatMap { avionPort.save(merged) }
        }

    // Supprime un avion
    fun delete(id: UUID): Mono<Unit> =
        get(id).flatMap { _ ->
            volPort.findAll()
                .filter { it.avionId == id }
                .filter { it.etat !in setOf(VolEtat.ARRIVE, VolEtat.ANNULE) }
                .hasElements()
                .flatMap { hasLockedFlights ->
                    if (hasLockedFlights)
                        Mono.error<Unit>(IllegalStateException("Impossible de supprimer l'avion : il est encore affecté à des vols actifs."))
                    else
                        avionPort.deleteById(id).thenReturn(Unit)
                }
        }

    // Affecte un hangar
    fun assignHangar(id: UUID, hangarId: UUID): Mono<Avion> =
        checkHangarExists(hangarId)
            .flatMap { get(id) }
            .flatMap { avion -> avionPort.save(avion.copy(hangarId = hangarId)) }

    // Retire le hangar
    fun unassignHangar(id: UUID): Mono<Avion> =
        get(id).flatMap { avionPort.save(it.copy(hangarId = null)) }

    // Vérifie l’existence du hangar
    private fun checkHangarExists(hangarId: UUID?): Mono<Boolean> =
        if (hangarId == null) {
            Mono.just(true)
        } else {
            hangarPort.existsById(hangarId)
                .flatMap { exists ->
                    if (exists) Mono.just(true)
                    else Mono.error(NotFoundException("Hangar $hangarId not found"))
                }
        }
}
