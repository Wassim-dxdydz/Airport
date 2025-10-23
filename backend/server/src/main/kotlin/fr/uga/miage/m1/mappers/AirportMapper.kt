package fr.uga.miage.m1.mappers

import fr.uga.miage.m1.responses.*
import fr.uga.miage.m1.models.*

fun Avion.toResponse() = AvionResponse(id, immatriculation, type, capacite, etat, hangarId)
fun Piste.toResponse() = PisteResponse(id, identifiant, longueurM, etat)
fun Hangar.toResponse() = HangarResponse(id = this.id, identifiant, capacite, etat)
fun Vol.toResponse() = VolResponse(id, numeroVol, origine, destination, heureDepart, heureArrivee, etat, avionId, createdAt, updatedAt)