package fr.uga.miage.m1.domain.service

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.VolEtat
import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.PisteEtat
import fr.uga.miage.m1.domain.model.Vol
import fr.uga.miage.m1.domain.port.AvionDataPort
import fr.uga.miage.m1.domain.port.VolDataPort
import fr.uga.miage.m1.domain.port.VolHistoryDataPort
import fr.uga.miage.m1.domain.model.VolHistory
import fr.uga.miage.m1.domain.port.PisteDataPort
import fr.uga.miage.m1.domain.validation.VolValidator
import fr.uga.miage.m1.exceptions.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class VolService(
    private val volPort: VolDataPort,
    private val avionPort: AvionDataPort,
    private val syncService: SharedVolSyncService,
    private val volHistoryPort: VolHistoryDataPort,
    private val statusStrategy: VolStatusStrategy = DefaultVolStatusStrategy,
    private val pistePort: PisteDataPort
) {

    fun list(): Flux<Vol> =
        volPort.findAll()

    fun get(id: UUID): Mono<Vol> =
        volPort.findById(id)
            .switchIfEmpty(Mono.error(NotFoundException("Vol $id not found")))

    fun create(vol: Vol): Mono<Vol> {
        VolValidator.validate(vol)
        val toSave = vol.copy(etat = VolEtat.PREVU)

        return volPort.save(toSave)
            .flatMap { saved ->
                logHistory(saved)
                    .then(syncService.pushToPartner(saved))
                    .thenReturn(saved)
            }
    }

    fun update(id: UUID, updated: Vol): Mono<Vol> =
        get(id).flatMap { current ->
            val merged = current.copy(
                origine = updated.origine,
                destination = updated.destination,
                heureDepart = updated.heureDepart,
                heureArrivee = updated.heureArrivee,
                etat = updated.etat,
                avionId = updated.avionId
            )

            VolValidator.validate(merged)
            volPort.save(merged)
        }.flatMap { saved ->
            logHistory(saved)
                .then(syncService.pushToPartner(saved))
                .thenReturn(saved)
        }

    fun updateBasicFields(id: UUID, updated: Vol): Mono<Vol> =
        get(id).flatMap { current ->
            val merged = current.copy(
                origine = updated.origine,
                destination = updated.destination,
                heureDepart = updated.heureDepart,
                heureArrivee = updated.heureArrivee
            )

            VolValidator.validate(merged)
            volPort.save(merged)
        }.flatMap { saved ->
            logHistory(saved)
                .then(syncService.pushToPartner(saved))
                .thenReturn(saved)
        }

    fun delete(id: UUID): Mono<Unit> =
        volPort.deleteById(id).thenReturn(Unit)

    fun assignAvion(id: UUID, avionId: UUID): Mono<Vol> =
        avionPort.findById(avionId)
            .switchIfEmpty(Mono.error(NotFoundException("Avion $avionId non trouvé")))
            .flatMap { get(id) }
            .flatMap { vol ->
                volPort.save(vol.copy(avionId = avionId))
            }

    fun unassignAvion(id: UUID): Mono<Vol> =
        get(id)
            .flatMap { volPort.save(it.copy(avionId = null)) }

    fun updateEtat(id: UUID, etat: VolEtat): Mono<Vol> =
        get(id).flatMap { current ->
            if (!statusStrategy.canTransition(current.etat, etat)) {
                return@flatMap Mono.error(
                    IllegalArgumentException("Transition de ${current.etat} vers $etat non autorisée")
                )
            }
            volPort.save(current.copy(etat = etat))
        }

    fun listByEtat(etat: VolEtat): Flux<Vol> =
        volPort.findByEtat(etat)

    private fun logHistory(vol: Vol) =
        volHistoryPort.save(
            VolHistory(volId = vol.id!!, etat = vol.etat)
        )

    fun assignPiste(volId: UUID, pisteId: UUID): Mono<Vol> =
        Mono.zip(
            volPort.findById(volId)
                .switchIfEmpty(Mono.error(NotFoundException("Vol $volId non trouvé"))),
            pistePort.findById(pisteId)
                .switchIfEmpty(Mono.error(NotFoundException("Piste $pisteId non trouvée")))
        ).flatMap { tuple ->
            val vol = tuple.t1
            val piste = tuple.t2

            if (piste.etat != PisteEtat.LIBRE) {
                return@flatMap Mono.error<Vol>(IllegalStateException("Piste déjà occupée"))
            }

            val updatedPiste = piste.copy(etat = PisteEtat.OCCUPEE)
            val updatedVol = vol.copy(pisteId = piste.id)

            pistePort.save(updatedPiste)
                .then(volPort.save(updatedVol))
        }

    fun releasePiste(volId: UUID): Mono<Vol> =
        volPort.findById(volId)
            .switchIfEmpty(Mono.error(NotFoundException("Vol $volId non trouvé")))
            .flatMap { vol ->
                val pisteId = vol.pisteId
                    ?: return@flatMap Mono.error<Vol>(IllegalStateException("Aucune piste affectée à ce vol"))

                pistePort.findById(pisteId)
                    .switchIfEmpty(Mono.error(NotFoundException("Piste $pisteId non trouvée")))
                    .flatMap { piste ->
                        val updatedPiste = piste.copy(etat = PisteEtat.LIBRE)
                        val updatedVol = vol.copy(pisteId = null)

                        pistePort.save(updatedPiste)
                            .then(volPort.save(updatedVol))
                    }
            }

    fun listByPiste(pisteId: UUID): Flux<Vol> =
        volPort.findByPisteId(pisteId)

    fun listDeparturesFrom(airportCode: String): Flux<Vol> =
        volPort.findByOrigine(airportCode)

    fun listArrivalsTo(airportCode: String): Flux<Vol> =
        volPort.findByDestination(airportCode)

    fun trafficFor(airportCode: String): Flux<Vol> =
        volPort.findByOrigine(airportCode)
            .mergeWith(volPort.findByDestination(airportCode))

}
