CREATE TABLE vol (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     numero_vol VARCHAR(20) UNIQUE NOT NULL,
     origine VARCHAR(100) NOT NULL,
     destination VARCHAR(100) NOT NULL,
     heure_depart TIMESTAMP NOT NULL,
     heure_arrivee TIMESTAMP NOT NULL,
     etat VARCHAR(20) NOT NULL,
     avion_id UUID NULL REFERENCES avion(id) ON DELETE SET NULL,
     created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vol_etat ON vol(etat);
CREATE INDEX idx_vol_avion ON vol(avion_id);
CREATE INDEX idx_vol_numero ON vol(numero_vol);
CREATE INDEX idx_vol_depart ON vol(heure_depart);
