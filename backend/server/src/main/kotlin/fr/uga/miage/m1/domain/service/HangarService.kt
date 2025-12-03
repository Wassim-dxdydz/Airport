package fr.uga.miage.m1.domain.service

import fr.uga.miage.m1.domain.model.Hangar
import fr.uga.miage.m1.domain.port.HangarDataPort
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.validation.HangarValidator
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
    // Liste tous les hangars
    fun list(): Flux<Hangar> =
        hangarPort.findAll()

    // Récupère un hangar par son id
    fun get(id: UUID): Mono<Hangar> =
        hangarPort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Hangar $id not found")))

    // Créer un hangar (avec validation)
    fun create(hangar: Hangar): Mono<Hangar> {
        HangarValidator.validate(hangar)
        return hangarPort.save(hangar)
    }

    // Met à jour un hangar
    fun update(id: UUID, updated: Hangar): Mono<Hangar> =
        get(id).flatMap { current ->

            val merged = current.copy(
                identifiant = updated.identifiant,
                capacite = updated.capacite,
                etat = updated.etat
            )

            HangarValidator.validate(merged)

            hangarPort.save(merged)
        }

    // Supprime un hangar
    fun delete(id: UUID): Mono<Unit> =
        get(id).flatMap { _ ->
            avionPort.findAll()
                .filter { it.hangarId == id }
                .hasElements()
                .flatMap { hasAvions ->
                    if (hasAvions)
                        Mono.error<Unit>(IllegalStateException("Impossible de supprimer le hangar : il contient encore des avions."))
                    else
                        hangarPort.deleteById(id).thenReturn(Unit)
                }
        }

    // Liste les avions d'un hangar
    fun listAvions(id: UUID) =
        avionPort.findAll()
            .filter { it.hangarId == id }
}
