CREATE TABLE hangar (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifiant VARCHAR(50) UNIQUE NOT NULL,
    capacite INT NOT NULL,
    etat VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE piste (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   identifiant VARCHAR(50) UNIQUE NOT NULL,
   longueur_m INT NOT NULL,
   etat VARCHAR(20) NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE avion (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   immatriculation VARCHAR(32) UNIQUE NOT NULL,
   type VARCHAR(80) NOT NULL,
   capacite INT NOT NULL,
   etat VARCHAR(20) NOT NULL,
   hangar_id UUID NULL REFERENCES hangar(id) ON DELETE SET NULL,
   created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_piste_etat ON piste(etat);
CREATE INDEX idx_hangar_identifiant ON hangar(identifiant);
CREATE INDEX idx_avion_etat ON avion(etat);
CREATE INDEX idx_avion_hangar ON avion(hangar_id);

CREATE TABLE IF NOT EXISTS vol (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_vol VARCHAR(50) NOT NULL UNIQUE,
    origine VARCHAR(3) NOT NULL,
    destination VARCHAR(3) NOT NULL,
    heure_depart TIMESTAMP NOT NULL,
    heure_arrivee TIMESTAMP NOT NULL,
    etat VARCHAR(50) NOT NULL,
    avion_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    FOREIGN KEY (avion_id) REFERENCES avion(id)
    );


CREATE INDEX idx_vol_etat ON vol(etat);
CREATE INDEX idx_vol_avion ON vol(avion_id);
CREATE INDEX idx_vol_numero ON vol(numero_vol);
CREATE INDEX idx_vol_depart ON vol(heure_depart);