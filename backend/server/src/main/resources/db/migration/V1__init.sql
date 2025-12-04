-- ============================
--   TABLE HANGAR
-- ============================
CREATE TABLE IF NOT EXISTS hangar (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifiant VARCHAR(50) UNIQUE NOT NULL,
    capacite INT NOT NULL,
    etat VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_hangar_identifiant ON hangar(identifiant);

-- ============================
--   TABLE PISTE
-- ============================
CREATE TABLE IF NOT EXISTS piste (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifiant VARCHAR(50) UNIQUE NOT NULL,
    longueur_m INT NOT NULL,
    etat VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_piste_etat ON piste(etat);

-- ============================
--   TABLE AVION
-- ============================
CREATE TABLE IF NOT EXISTS avion (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    immatriculation VARCHAR(32) UNIQUE NOT NULL,
    type VARCHAR(80) NOT NULL,
    capacite INT NOT NULL,
    etat VARCHAR(20) NOT NULL,
    hangar_id UUID NULL REFERENCES hangar(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_avion_etat ON avion(etat);
CREATE INDEX IF NOT EXISTS idx_avion_hangar ON avion(hangar_id);

-- ============================
--   TABLE VOL
-- ============================
CREATE TABLE IF NOT EXISTS vol (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_vol VARCHAR(50) NOT NULL UNIQUE,
    origine VARCHAR(3) NOT NULL,
    destination VARCHAR(3) NOT NULL,
    heure_depart TIMESTAMP NOT NULL,
    heure_arrivee TIMESTAMP NOT NULL,
    etat VARCHAR(50) NOT NULL,
    avion_id UUID REFERENCES avion(id) ON DELETE SET NULL,
    piste_id UUID NULL REFERENCES piste(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_vol_etat   ON vol(etat);
CREATE INDEX IF NOT EXISTS idx_vol_avion  ON vol(avion_id);
CREATE INDEX IF NOT EXISTS idx_vol_numero ON vol(numero_vol);
CREATE INDEX IF NOT EXISTS idx_vol_depart ON vol(heure_depart);
CREATE INDEX IF NOT EXISTS idx_vol_piste  ON vol(piste_id);

-- ============================
--   TABLE VOL HISTORY
-- ============================
CREATE TABLE IF NOT EXISTS vol_history (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vol_id UUID NOT NULL REFERENCES vol(id) ON DELETE CASCADE,
    etat VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_vol_history_vol ON vol_history(vol_id);
CREATE INDEX IF NOT EXISTS idx_vol_history_etat ON vol_history(etat);
