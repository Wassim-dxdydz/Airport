CREATE TABLE IF NOT EXISTS hangar (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    identifiant VARCHAR(50) UNIQUE NOT NULL,
    capacite INT NOT NULL,
    etat VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS piste (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    identifiant VARCHAR(50) UNIQUE NOT NULL,
    longueur_m INT NOT NULL,
    etat VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS avion (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    immatriculation VARCHAR(32) UNIQUE NOT NULL,
    type VARCHAR(80) NOT NULL,
    capacite INT NOT NULL,
    etat VARCHAR(20) NOT NULL,
    hangar_id UUID NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS vol (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    numero_vol VARCHAR(50) NOT NULL UNIQUE,
    origine VARCHAR(3) NOT NULL,
    destination VARCHAR(3) NOT NULL,
    heure_depart TIMESTAMP NOT NULL,
    heure_arrivee TIMESTAMP NOT NULL,
    etat VARCHAR(50) NOT NULL,
    avion_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (avion_id) REFERENCES avion(id)
    );
