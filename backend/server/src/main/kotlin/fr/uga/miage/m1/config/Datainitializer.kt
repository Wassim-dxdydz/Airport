package fr.uga.miage.m1.config

import backend.common.src.main.kotlin.fr.uga.miage.m1.enums.*
import fr.uga.miage.m1.persistence.entity.*
import fr.uga.miage.m1.persistence.repository.*
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Configuration
class DataInitializer {

    data class VolSpec(
        var num: String = "",
        var origine: String = "",
        var destination: String = "",
        var depHours: Int = 0,
        var duration: Long = 0,
        var etat: VolEtat = VolEtat.PREVU,
        var avionId: UUID? = null,
        var pisteId: UUID? = null
    )

    private fun vol(init: VolSpec.() -> Unit): VolSpec = VolSpec().apply(init)

    fun initDatabaseReactive(
        hangarRepo: HangarRepository,
        avionRepo: AvionRepository,
        pisteRepo: PisteRepository,
        volRepo: VolRepository,
        volHistoryRepo: VolHistoryRepository,
        passengerRepo: PassengerRepository,
        checkInRepo: CheckInRepository
    ): Mono<Void> {

        val reset = checkInRepo.deleteAll()
            .then(passengerRepo.deleteAll())
            .then(volHistoryRepo.deleteAll())
            .then(volRepo.deleteAll())
            .then(avionRepo.deleteAll())
            .then(pisteRepo.deleteAll())
            .then(hangarRepo.deleteAll())

        return reset.then(
            Mono.defer {

                val hangarsData = listOf(
                    HangarEntity(null, "H1", 5, HangarEtat.DISPONIBLE),
                    HangarEntity(null, "H2", 6, HangarEtat.DISPONIBLE),
                    HangarEntity(null, "H3", 4, HangarEtat.MAINTENANCE),
                    HangarEntity(null, "A1", 3, HangarEtat.DISPONIBLE),
                    HangarEntity(null, "B2", 8, HangarEtat.DISPONIBLE),
                    HangarEntity(null, "C3", 2, HangarEtat.PLEIN),
                    HangarEntity(null, "D4", 7, HangarEtat.DISPONIBLE),
                    HangarEntity(null, "E5", 5, HangarEtat.MAINTENANCE)
                )

                hangarRepo.saveAll(hangarsData).collectList().flatMap { hangars ->

                    val avionsData = listOf(
                        AvionEntity(null, "F-ABCD", "A320", 180, AvionEtat.DISPONIBLE, hangars[0].id),
                        AvionEntity(null, "F-BCDE", "A320", 160, AvionEtat.DISPONIBLE, hangars[0].id),
                        AvionEntity(null, "HB-JCA", "B737", 140, AvionEtat.DISPONIBLE, hangars[1].id),
                        AvionEntity(null, "F-ZZZZ", "A220", 120, AvionEtat.DISPONIBLE, hangars[1].id),
                        AvionEntity(null, "N12345", "G650", 12, AvionEtat.DISPONIBLE, hangars[3].id),
                        AvionEntity(null, "F-AAAA", "ATR72", 72, AvionEtat.MAINTENANCE, hangars[3].id),
                        AvionEntity(null, "D-ABCD", "A350", 320, AvionEtat.MAINTENANCE, hangars[3].id),
                        AvionEntity(null, "F-QWER", "B777", 380, AvionEtat.EN_VOL, null),
                        AvionEntity(null, "F-TTTT", "A330", 260, AvionEtat.EN_VOL, null),
                        AvionEntity(null, "HB-XYZ", "A319", 144, AvionEtat.EN_VOL, null),
                        AvionEntity(null, "N54321", "C525", 6, AvionEtat.EN_VOL, null),
                        AvionEntity(null, "F-PLMA", "A320", 180, AvionEtat.DISPONIBLE, hangars[4].id),
                        AvionEntity(null, "F-BBB2", "A318", 108, AvionEtat.DISPONIBLE, hangars[5].id),
                        AvionEntity(null, "F-CCCC", "A320", 180, AvionEtat.DISPONIBLE, hangars[5].id),
                        AvionEntity(null, "F-DDDD", "B787", 280, AvionEtat.DISPONIBLE, hangars[6].id)
                    )

                    avionRepo.saveAll(avionsData).collectList().flatMap { avions ->

                        val pistesData = listOf(
                            PisteEntity(null, "09L", 3500, PisteEtat.LIBRE),
                            PisteEntity(null, "09R", 3600, PisteEtat.OCCUPEE),
                            PisteEntity(null, "12L", 4200, PisteEtat.LIBRE),
                            PisteEntity(null, "12R", 4100, PisteEtat.MAINTENANCE),
                            PisteEntity(null, "A3", 3000, PisteEtat.OCCUPEE)
                        )

                        pisteRepo.saveAll(pistesData).collectList().flatMap { pistes ->

                            val now = LocalDateTime.now()

                            val volSpecs = listOf(
                                vol { num="AF201"; origine="ATL"; destination="CDG"; depHours=1; duration=540; etat=VolEtat.PREVU; avionId=avions[0].id },
                                vol { num="AF203"; origine="ATL"; destination="LHR"; depHours=3; duration=480; etat=VolEtat.EN_ATTENTE; avionId=avions[1].id },
                                vol { num="AF204"; origine="AMS"; destination="ATL"; depHours=4; duration=520; etat=VolEtat.PREVU },
                                vol { num="AF205"; origine="ATL"; destination="MAD"; depHours=2; duration=450; etat=VolEtat.EMBARQUEMENT; avionId=avions[2].id; pisteId=pistes[0].id },
                                vol { num="AF206"; origine="FRA"; destination="ATL"; depHours=5; duration=550; etat=VolEtat.DECOLLE; avionId=avions[3].id; pisteId=pistes[1].id },
                                vol { num="AF207"; origine="ATL"; destination="BRU"; depHours=6; duration=490; etat=VolEtat.EN_VOL; avionId=avions[7].id },
                                vol { num="AF208"; origine="MXP"; destination="ATL"; depHours=1; duration=610; etat=VolEtat.ARRIVE; avionId=avions[8].id; pisteId=pistes[4].id },
                                vol { num="AF209"; origine="ATL"; destination="LUX"; depHours=2; duration=400; etat=VolEtat.EN_VOL; avionId=avions[9].id },
                                vol { num="AF210"; origine="OPO"; destination="ATL"; depHours=4; duration=560; etat=VolEtat.EN_VOL; avionId=avions[10].id },
                                vol { num="AF211"; origine="ATL"; destination="ATH"; depHours=3; duration=530; etat=VolEtat.PREVU; avionId=avions[11].id },
                                vol { num="AF212"; origine="MAD"; destination="ATL"; depHours=7; duration=530; etat=VolEtat.ANNULE },
                                vol { num="AF220"; origine="ATL"; destination="CDG"; depHours=2; duration=540; etat=VolEtat.EN_ATTENTE; avionId=avions[12].id },
                                vol { num="AF221"; origine="ATL"; destination="CDG"; depHours=3; duration=540; etat=VolEtat.PREVU },
                                vol { num="AF222"; origine="ATL"; destination="CDG"; depHours=4; duration=540; etat=VolEtat.EMBARQUEMENT; avionId=avions[13].id; pisteId=pistes[2].id },
                                vol { num="AF223"; origine="ATL"; destination="CDG"; depHours=5; duration=540; etat=VolEtat.PREVU; avionId=avions[14].id },
                                vol { num="AF224"; origine="ATL"; destination="CDG"; depHours=6; duration=540; etat=VolEtat.EN_ATTENTE; avionId=avions[4].id }
                            )

                            val volEntities = volSpecs.map {
                                VolEntity(
                                    id = null,
                                    numeroVol = it.num,
                                    origine = it.origine,
                                    destination = it.destination,
                                    heureDepart = now.plusHours(it.depHours.toLong()),
                                    heureArrivee = now.plusHours(it.depHours.toLong()).plusMinutes(it.duration),
                                    etat = it.etat,
                                    avionId = it.avionId,
                                    pisteId = it.pisteId
                                )
                            }

                            volRepo.saveAll(volEntities).collectList().flatMap { vols ->

                                val passengersData = listOf(
                                    PassengerEntity(null, "Jean", "Dupont", "jean.dupont@email.com", "+33612345678"),
                                    PassengerEntity(null, "Marie", "Martin", "marie.martin@email.com", "+33698765432"),
                                    PassengerEntity(null, "Pierre", "Bernard", "pierre.bernard@email.com", "+33611223344"),
                                    PassengerEntity(null, "Sophie", "Dubois", "sophie.dubois@email.com", null),
                                    PassengerEntity(null, "Luc", "Thomas", "luc.thomas@email.com", "+33645678901"),
                                    PassengerEntity(null, "Emma", "Robert", "emma.robert@email.com", "+33656789012"),
                                    PassengerEntity(null, "Antoine", "Petit", "antoine.petit@email.com", "+33667890123"),
                                    PassengerEntity(null, "Julie", "Richard", "julie.richard@email.com", null),
                                    PassengerEntity(null, "Marc", "Durand", "marc.durand@email.com", "+33678901234"),
                                    PassengerEntity(null, "Claire", "Moreau", "claire.moreau@email.com", "+33689012345"),
                                    PassengerEntity(null, "David", "Laurent", "david.laurent@email.com", "+33690123456"),
                                    PassengerEntity(null, "Isabelle", "Simon", "isabelle.simon@email.com", null),
                                    PassengerEntity(null, "François", "Michel", "francois.michel@email.com", "+33601234567"),
                                    PassengerEntity(null, "Nathalie", "Lefebvre", "nathalie.lefebvre@email.com", "+33612345670"),
                                    PassengerEntity(null, "Olivier", "Leroy", "olivier.leroy@email.com", null)
                                )

                                passengerRepo.saveAll(passengersData).collectList().flatMap { passengers ->

                                    val checkInsData = listOf(
                                        CheckInEntity(null, passengers[0].id!!, vols[0].id!!, "1A", now.minusHours(3)),
                                        CheckInEntity(null, passengers[1].id!!, vols[0].id!!, "1B", now.minusHours(3)),
                                        CheckInEntity(null, passengers[2].id!!, vols[0].id!!, "12C", now.minusHours(2)),
                                        CheckInEntity(null, passengers[3].id!!, vols[1].id!!, "5A", now.minusHours(4)),
                                        CheckInEntity(null, passengers[4].id!!, vols[1].id!!, "5B", now.minusHours(4)),
                                        CheckInEntity(null, passengers[5].id!!, vols[3].id!!, "10F", now.minusHours(2)),
                                        CheckInEntity(null, passengers[6].id!!, vols[3].id!!, "10E", now.minusHours(2)),
                                        CheckInEntity(null, passengers[7].id!!, vols[3].id!!, "15A", now.minusHours(1)),
                                        CheckInEntity(null, passengers[8].id!!, vols[4].id!!, "8C", now.minusHours(3)),
                                        CheckInEntity(null, passengers[9].id!!, vols[4].id!!, "8D", now.minusHours(3)),
                                        CheckInEntity(null, passengers[10].id!!, vols[5].id!!, "25A", now.minusHours(5)),
                                        CheckInEntity(null, passengers[11].id!!, vols[5].id!!, "25B", now.minusHours(5)),
                                        CheckInEntity(null, passengers[12].id!!, vols[9].id!!, "20F", now.minusHours(1)),
                                        CheckInEntity(null, passengers[13].id!!, vols[11].id!!, "7C", now.minusHours(2)),
                                        CheckInEntity(null, passengers[14].id!!, vols[11].id!!, "7D", now.minusHours(2)),
                                        CheckInEntity(null, passengers[0].id!!, vols[13].id!!, "2A", now.minusHours(2)),
                                        CheckInEntity(null, passengers[1].id!!, vols[13].id!!, "2B", now.minusHours(2)),
                                        CheckInEntity(null, passengers[5].id!!, vols[15].id!!, "1C", now.minusHours(1))
                                    )

                                    checkInRepo.saveAll(checkInsData).collectList().flatMap {

                                        val history = listOf(
                                            VolHistoryEntity(null, vols[0].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[1].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[1].id!!, VolEtat.EN_ATTENTE),
                                            VolHistoryEntity(null, vols[2].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[3].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[3].id!!, VolEtat.EN_ATTENTE),
                                            VolHistoryEntity(null, vols[3].id!!, VolEtat.EMBARQUEMENT),
                                            VolHistoryEntity(null, vols[4].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[4].id!!, VolEtat.EN_ATTENTE),
                                            VolHistoryEntity(null, vols[4].id!!, VolEtat.EMBARQUEMENT),
                                            VolHistoryEntity(null, vols[4].id!!, VolEtat.DECOLLE),
                                            VolHistoryEntity(null, vols[5].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[5].id!!, VolEtat.EN_ATTENTE),
                                            VolHistoryEntity(null, vols[5].id!!, VolEtat.EMBARQUEMENT),
                                            VolHistoryEntity(null, vols[5].id!!, VolEtat.DECOLLE),
                                            VolHistoryEntity(null, vols[5].id!!, VolEtat.EN_VOL),
                                            VolHistoryEntity(null, vols[6].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[6].id!!, VolEtat.EN_ATTENTE),
                                            VolHistoryEntity(null, vols[6].id!!, VolEtat.EMBARQUEMENT),
                                            VolHistoryEntity(null, vols[6].id!!, VolEtat.DECOLLE),
                                            VolHistoryEntity(null, vols[6].id!!, VolEtat.EN_VOL),
                                            VolHistoryEntity(null, vols[6].id!!, VolEtat.ARRIVE),
                                            VolHistoryEntity(null, vols[7].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[7].id!!, VolEtat.EN_ATTENTE),
                                            VolHistoryEntity(null, vols[7].id!!, VolEtat.EMBARQUEMENT),
                                            VolHistoryEntity(null, vols[7].id!!, VolEtat.DECOLLE),
                                            VolHistoryEntity(null, vols[7].id!!, VolEtat.EN_VOL),
                                            VolHistoryEntity(null, vols[8].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[8].id!!, VolEtat.EN_ATTENTE),
                                            VolHistoryEntity(null, vols[8].id!!, VolEtat.EMBARQUEMENT),
                                            VolHistoryEntity(null, vols[8].id!!, VolEtat.DECOLLE),
                                            VolHistoryEntity(null, vols[8].id!!, VolEtat.EN_VOL),
                                            VolHistoryEntity(null, vols[9].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[10].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[10].id!!, VolEtat.ANNULE),
                                            VolHistoryEntity(null, vols[11].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[11].id!!, VolEtat.EN_ATTENTE),
                                            VolHistoryEntity(null, vols[12].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[13].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[13].id!!, VolEtat.EN_ATTENTE),
                                            VolHistoryEntity(null, vols[13].id!!, VolEtat.EMBARQUEMENT),
                                            VolHistoryEntity(null, vols[14].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[15].id!!, VolEtat.PREVU),
                                            VolHistoryEntity(null, vols[15].id!!, VolEtat.EN_ATTENTE)
                                        )

                                        volHistoryRepo.saveAll(history).then()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    @Bean
    fun initDatabase(
        hangarRepo: HangarRepository,
        avionRepo: AvionRepository,
        pisteRepo: PisteRepository,
        volRepo: VolRepository,
        volHistoryRepo: VolHistoryRepository,
        passengerRepo: PassengerRepository,
        checkInRepo: CheckInRepository
    ) = CommandLineRunner {
        initDatabaseReactive(
            hangarRepo,
            avionRepo,
            pisteRepo,
            volRepo,
            volHistoryRepo,
            passengerRepo,
            checkInRepo
        )
            .doOnSuccess {
                println(" Initialisation terminée :")
                println("   - 8 hangars")
                println("   - 15 avions")
                println("   - 5 pistes")
                println("   - 16 vols")
                println("   - 15 passagers")
                println("   - 18 check-ins")
                println("   - Historiques de vols")
            }
            .subscribe()
    }
}
