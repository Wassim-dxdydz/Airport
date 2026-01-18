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
        volHistoryRepo: VolHistoryRepository
    ): Mono<Void> {

        val reset = volHistoryRepo.deleteAll()
            .then(volRepo.deleteAll())
            .then(avionRepo.deleteAll())
            .then(pisteRepo.deleteAll())
            .then(hangarRepo.deleteAll())

        return reset.then(
            Mono.defer {

                // HANGARS
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

                    // AVIONS
                    val avionsData = listOf(
                        AvionEntity(null, "F-ABCD", "A320", 180, AvionEtat.DISPONIBLE, hangars[0].id), // H1
                        AvionEntity(null, "F-BCDE", "A320", 160, AvionEtat.DISPONIBLE, hangars[0].id), // H1
                        AvionEntity(null, "HB-JCA", "B737", 140, AvionEtat.DISPONIBLE, hangars[1].id), // H2
                        AvionEntity(null, "F-ZZZZ", "A220", 120, AvionEtat.DISPONIBLE, hangars[1].id), // H2

                        AvionEntity(null, "N12345", "Gulfstream G650", 12, AvionEtat.DISPONIBLE, hangars[3].id), // A1

                        AvionEntity(null, "F-AAAA", "ATR-72", 72, AvionEtat.MAINTENANCE, hangars[3].id), // A1
                        AvionEntity(null, "D-ABCD", "A350", 320, AvionEtat.MAINTENANCE, hangars[3].id),  // A1

                        AvionEntity(null, "F-QWER", "B777", 380, AvionEtat.EN_VOL, null),
                        AvionEntity(null, "F-TTTT", "A330", 260, AvionEtat.EN_VOL, null),
                        AvionEntity(null, "HB-XYZ", "A319", 144, AvionEtat.EN_VOL, null),
                        AvionEntity(null, "N54321", "Cessna 525", 6, AvionEtat.EN_VOL, null),

                        AvionEntity(null, "F-PLMA", "A320", 180, AvionEtat.DISPONIBLE, hangars[4].id), // B2

                        // Fill C3 (capacity=2, etat=PLEIN)
                        AvionEntity(null, "F-BBB2", "A318", 110, AvionEtat.DISPONIBLE, hangars[5].id), // C3
                        AvionEntity(null, "F-CCCC", "A320neo", 190, AvionEtat.DISPONIBLE, hangars[5].id), // C3

                        AvionEntity(null, "F-DDDD", "B787", 280, AvionEtat.DISPONIBLE, hangars[6].id) // D4
                    )


                    avionRepo.saveAll(avionsData).collectList().flatMap { avions ->

                        // PISTES
                        val pistesData = listOf(
                            PisteEntity(null, "09L", 3500, PisteEtat.LIBRE),
                            PisteEntity(null, "09R", 3600, PisteEtat.OCCUPEE),
                            PisteEntity(null, "12L", 4200, PisteEtat.LIBRE),
                            PisteEntity(null, "12R", 4100, PisteEtat.MAINTENANCE),
                            PisteEntity(null, "A3", 3000, PisteEtat.OCCUPEE)
                        )

                        pisteRepo.saveAll(pistesData).collectList().flatMap { pistes ->

                            val now = LocalDateTime.now()

                            // VOLS
                            val volSpecs = listOf(
                                vol { num="AF201"; origine="ATL"; destination="CDG"; depHours=1; duration=540; etat=VolEtat.PREVU; avionId=avions[0].id },
                                vol { num="AF203"; origine="ATL"; destination="LHR"; depHours=3; duration=480; etat=VolEtat.EN_ATTENTE; avionId=avions[1].id },

                                vol { num="AF204"; origine="AMS"; destination="ATL"; depHours=4; duration=520; etat=VolEtat.PREVU }, // no avion yet ok

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

                                // HISTORY
                                val history = listOf(
                                    // 0 - AF201 (PREVU)
                                    VolHistoryEntity(null, vols[0].id!!, VolEtat.PREVU),

                                    // 1 - AF203 (EN_ATTENTE): PREVU -> EN_ATTENTE
                                    VolHistoryEntity(null, vols[1].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[1].id!!, VolEtat.EN_ATTENTE),

                                    // 2 - AF204 (PREVU)
                                    VolHistoryEntity(null, vols[2].id!!, VolEtat.PREVU),

                                    // 3 - AF205 (EMBARQUEMENT): PREVU -> EN_ATTENTE -> EMBARQUEMENT
                                    VolHistoryEntity(null, vols[3].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[3].id!!, VolEtat.EN_ATTENTE),
                                    VolHistoryEntity(null, vols[3].id!!, VolEtat.EMBARQUEMENT),

                                    // 4 - AF206 (DECOLLE): PREVU -> EN_ATTENTE -> EMBARQUEMENT -> DECOLLE
                                    VolHistoryEntity(null, vols[4].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[4].id!!, VolEtat.EN_ATTENTE),
                                    VolHistoryEntity(null, vols[4].id!!, VolEtat.EMBARQUEMENT),
                                    VolHistoryEntity(null, vols[4].id!!, VolEtat.DECOLLE),

                                    // 5 - AF207 (EN_VOL): PREVU -> EN_ATTENTE -> EMBARQUEMENT -> DECOLLE -> EN_VOL
                                    VolHistoryEntity(null, vols[5].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[5].id!!, VolEtat.EN_ATTENTE),
                                    VolHistoryEntity(null, vols[5].id!!, VolEtat.EMBARQUEMENT),
                                    VolHistoryEntity(null, vols[5].id!!, VolEtat.DECOLLE),
                                    VolHistoryEntity(null, vols[5].id!!, VolEtat.EN_VOL),

                                    // 6 - AF208 (ARRIVE): PREVU -> EN_ATTENTE -> EMBARQUEMENT -> DECOLLE -> EN_VOL -> ARRIVE
                                    VolHistoryEntity(null, vols[6].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[6].id!!, VolEtat.EN_ATTENTE),
                                    VolHistoryEntity(null, vols[6].id!!, VolEtat.EMBARQUEMENT),
                                    VolHistoryEntity(null, vols[6].id!!, VolEtat.DECOLLE),
                                    VolHistoryEntity(null, vols[6].id!!, VolEtat.EN_VOL),
                                    VolHistoryEntity(null, vols[6].id!!, VolEtat.ARRIVE),

                                    // 7 - AF209 (EN_VOL): PREVU -> EN_ATTENTE -> EMBARQUEMENT -> DECOLLE -> EN_VOL
                                    VolHistoryEntity(null, vols[7].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[7].id!!, VolEtat.EN_ATTENTE),
                                    VolHistoryEntity(null, vols[7].id!!, VolEtat.EMBARQUEMENT),
                                    VolHistoryEntity(null, vols[7].id!!, VolEtat.DECOLLE),
                                    VolHistoryEntity(null, vols[7].id!!, VolEtat.EN_VOL),

                                    // 8 - AF210 (EN_VOL): PREVU -> EN_ATTENTE -> EMBARQUEMENT -> DECOLLE -> EN_VOL
                                    VolHistoryEntity(null, vols[8].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[8].id!!, VolEtat.EN_ATTENTE),
                                    VolHistoryEntity(null, vols[8].id!!, VolEtat.EMBARQUEMENT),
                                    VolHistoryEntity(null, vols[8].id!!, VolEtat.DECOLLE),
                                    VolHistoryEntity(null, vols[8].id!!, VolEtat.EN_VOL),

                                    // 9 - AF211 (PREVU)
                                    VolHistoryEntity(null, vols[9].id!!, VolEtat.PREVU),

                                    // 10 - AF212 (ANNULE): PREVU -> ANNULE
                                    VolHistoryEntity(null, vols[10].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[10].id!!, VolEtat.ANNULE),

                                    // 11 - AF220 (EN_ATTENTE): PREVU -> EN_ATTENTE
                                    VolHistoryEntity(null, vols[11].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[11].id!!, VolEtat.EN_ATTENTE),

                                    // 12 - AF221 (PREVU)
                                    VolHistoryEntity(null, vols[12].id!!, VolEtat.PREVU),

                                    // 13 - AF222 (EMBARQUEMENT): PREVU -> EN_ATTENTE -> EMBARQUEMENT
                                    VolHistoryEntity(null, vols[13].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[13].id!!, VolEtat.EN_ATTENTE),
                                    VolHistoryEntity(null, vols[13].id!!, VolEtat.EMBARQUEMENT),

                                    // 14 - AF223 (PREVU)
                                    VolHistoryEntity(null, vols[14].id!!, VolEtat.PREVU),

                                    // 15 - AF224 (EN_ATTENTE): PREVU -> EN_ATTENTE
                                    VolHistoryEntity(null, vols[15].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[15].id!!, VolEtat.EN_ATTENTE)
                                )

                                volHistoryRepo.saveAll(history).then()
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
        volHistoryRepo: VolHistoryRepository
    ) = CommandLineRunner {
        initDatabaseReactive(
            hangarRepo,
            avionRepo,
            pisteRepo,
            volRepo,
            volHistoryRepo
        )
            .doOnSuccess {
                println("Initialisation terminée :")
                println("8 hangars, 15 avions, 5 pistes, 16 vols, 9 historiques")
            }
            .subscribe()
    }
}
