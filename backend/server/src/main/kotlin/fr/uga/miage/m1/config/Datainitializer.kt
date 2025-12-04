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

    @Bean
    fun initDatabase(
        hangarRepo: HangarRepository,
        avionRepo: AvionRepository,
        pisteRepo: PisteRepository,
        volRepo: VolRepository,
        volHistoryRepo: VolHistoryRepository
    ) = CommandLineRunner {

        println("Initialisation de la base de données (R2DBC)…")
        // On supprime tout d'abord
        val reset = volHistoryRepo.deleteAll()
            .then(volRepo.deleteAll())
            .then(avionRepo.deleteAll())
            .then(pisteRepo.deleteAll())
            .then(hangarRepo.deleteAll())

        reset.then(
            Mono.defer {

                //HANGARS — 8
                val hangarsData = listOf(
                    HangarEntity(null, "H1", 5, HangarEtat.DISPONIBLE),
                    HangarEntity(null, "H2", 6, HangarEtat.DISPONIBLE),
                    HangarEntity(null, "H3", 4, HangarEtat.MAINTENANCE),
                    HangarEntity(null, "A1", 3, HangarEtat.DISPONIBLE),
                    HangarEntity(null, "B2", 8, HangarEtat.DISPONIBLE),
                    HangarEntity(null, "C3", 10, HangarEtat.PLEIN),
                    HangarEntity(null, "D4", 7, HangarEtat.DISPONIBLE),
                    HangarEntity(null, "E5", 5, HangarEtat.MAINTENANCE)
                )

                hangarRepo.saveAll(hangarsData).collectList().flatMap { hangars ->

                    //AVIONS — 15
                    val avionsData = listOf(
                        AvionEntity(null, "F-ABCD", "A320", 180, AvionEtat.EN_SERVICE, hangars[0].id),
                        AvionEntity(null, "F-BCDE", "A320", 160, AvionEtat.MAINTENANCE, hangars[2].id),
                        AvionEntity(null, "HB-JCA", "B737", 140, AvionEtat.EN_SERVICE, null),
                        AvionEntity(null, "F-ZZZZ", "A220", 120, AvionEtat.EN_SERVICE, null),
                        AvionEntity(null, "N12345", "Gulfstream G650", 12, AvionEtat.HORS_SERVICE, null),
                        AvionEntity(null, "F-AAAA", "ATR-72", 72, AvionEtat.MAINTENANCE, hangars[1].id),
                        AvionEntity(null, "D-ABCD", "A350", 320, AvionEtat.EN_SERVICE, null),
                        AvionEntity(null, "F-QWER", "B777", 380, AvionEtat.EN_SERVICE, null),
                        AvionEntity(null, "F-TTTT", "A330", 260, AvionEtat.EN_SERVICE, hangars[4].id),
                        AvionEntity(null, "HB-XYZ", "A319", 144, AvionEtat.EN_SERVICE, null),
                        AvionEntity(null, "N54321", "Cessna 525", 6, AvionEtat.EN_SERVICE, null),
                        AvionEntity(null, "F-PLMA", "A320", 180, AvionEtat.EN_SERVICE, null),
                        AvionEntity(null, "F-BBB2", "A318", 110, AvionEtat.MAINTENANCE, hangars[7].id),
                        AvionEntity(null, "F-CCCC", "A320neo", 190, AvionEtat.EN_SERVICE, null),
                        AvionEntity(null, "F-DDDD", "B787", 280, AvionEtat.HORS_SERVICE, null)
                    )

                    avionRepo.saveAll(avionsData).collectList().flatMap { avions ->

                        //PISTES — 5
                        val pistesData = listOf(
                            PisteEntity(null, "09L", 3500, PisteEtat.LIBRE),
                            PisteEntity(null, "09R", 3600, PisteEtat.OCCUPEE),
                            PisteEntity(null, "12L", 4200, PisteEtat.LIBRE),
                            PisteEntity(null, "12R", 4100, PisteEtat.MAINTENANCE),
                            PisteEntity(null, "A3", 3000, PisteEtat.LIBRE)
                        )

                        pisteRepo.saveAll(pistesData).collectList().flatMap { pistes ->

                            //VOLS — 12
                            val now = LocalDateTime.now()

                            fun vol(
                                num: String,
                                o: String,
                                d: String,
                                depH: Int,
                                dur: Long,
                                etat: VolEtat,
                                avion: UUID?,
                                piste: UUID?
                            ) =
                                VolEntity(
                                    id = null,
                                    numeroVol = num,
                                    origine = o,
                                    destination = d,
                                    heureDepart = now.plusHours(depH.toLong()),
                                    heureArrivee = now.plusHours(depH.toLong()).plusMinutes(dur),
                                    etat = etat,
                                    avionId = avion,
                                    pisteId = piste
                                )

                            val volsData = listOf(
                                vol("AF201", "ATL", "CDG", 1, 540, VolEtat.PREVU, avions[0].id, null),
                                vol("AF203", "ATL", "LHR", 3, 480, VolEtat.EMBARQUEMENT, avions[2].id, pistes[0].id),
                                vol("AF204", "AMS", "ATL", 4, 520, VolEtat.PREVU, null, null),
                                vol("AF205", "ATL", "MAD", 2, 450, VolEtat.EN_ATTENTE, avions[4].id, pistes[2].id),
                                vol("AF206", "FRA", "ATL", 5, 550, VolEtat.EMBARQUEMENT, avions[6].id, pistes[0].id),
                                vol("AF207", "ATL", "BRU", 6, 490, VolEtat.PREVU, null, null),
                                vol("AF208", "MXP", "ATL", 1, 610, VolEtat.DECOLLE, avions[7].id, pistes[0].id),
                                vol("AF209", "ATL", "LUX", 2, 400, VolEtat.EN_VOL, avions[8].id, pistes[2].id),
                                vol("AF210", "OPO", "ATL", 4, 560, VolEtat.ARRIVE, avions[11].id, null),
                                vol("AF211", "ATL", "ATH", 3, 530, VolEtat.PREVU, null, null),
                                vol("AF212", "MAD", "ATL", 7, 530, VolEtat.ANNULE, null, null),
                                vol("AF220", "ATL", "CDG", 2, 540, VolEtat.PREVU, avions[3].id, null),
                                vol("AF221", "ATL", "CDG", 3, 540, VolEtat.EN_ATTENTE, avions[5].id, pistes[1].id),
                                vol("AF222", "ATL", "CDG", 4, 540, VolEtat.EMBARQUEMENT, avions[9].id, pistes[2].id),
                                vol("AF223", "ATL", "CDG", 5, 540, VolEtat.PREVU, null, null),
                                vol("AF224", "ATL", "CDG", 6, 540, VolEtat.EN_ATTENTE, avions[10].id, null)
                            )

                            volRepo.saveAll(volsData).collectList().flatMap { vols ->

                                // HISTORY for 5 vols
                                val history = listOf(
                                    VolHistoryEntity(null, vols[1].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[1].id!!, VolEtat.EMBARQUEMENT),

                                    VolHistoryEntity(null, vols[3].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[3].id!!, VolEtat.EN_ATTENTE),

                                    VolHistoryEntity(null, vols[4].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[4].id!!, VolEtat.EMBARQUEMENT),

                                    VolHistoryEntity(null, vols[5].id!!, VolEtat.PREVU),

                                    VolHistoryEntity(null, vols[7].id!!, VolEtat.PREVU),
                                    VolHistoryEntity(null, vols[7].id!!, VolEtat.EN_VOL)
                                )


                                volHistoryRepo.saveAll(history).then()
                            }
                        }
                    }
                }
            }
        ).doOnSuccess {
            println(" Initialisation terminée :")
            println(" Les 8 hangars ont été crées")
            println(" Les 15 avions ont été crées")
            println(" Les 5 pistes ont été crées")
            println(" Les 12 vols (5 avec historique) ont été crées")
        }.subscribe()
    }
}
