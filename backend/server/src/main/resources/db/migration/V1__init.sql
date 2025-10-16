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
