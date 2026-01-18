-- ============================
-- HANGARS (8)
-- ============================
INSERT INTO hangar (identifiant, capacite, etat) VALUES
('H1', 5, 'DISPONIBLE'),
('H2', 6, 'DISPONIBLE'),
('H3', 4, 'MAINTENANCE'),
('A1', 3, 'DISPONIBLE'),
('B2', 8, 'DISPONIBLE'),
('C3', 2, 'PLEIN'),
('D4', 7, 'DISPONIBLE'),
('E5', 5, 'MAINTENANCE');

-- ============================
-- PISTES (5)
-- ============================
INSERT INTO piste (identifiant, longueur_m, etat) VALUES
('09L', 3500, 'LIBRE'),
('09R', 3600, 'OCCUPEE'),
('12L', 4200, 'LIBRE'),
('12R', 4100, 'MAINTENANCE'),
('A3', 3000, 'OCCUPEE');

-- ============================
-- AVIONS (15)
-- ============================
INSERT INTO avion (immatriculation, type, capacite, etat, hangar_id) VALUES
('F-ABCD', 'A320', 180, 'DISPONIBLE', (SELECT id FROM hangar WHERE identifiant='H1')),
('F-BCDE', 'A320', 160, 'DISPONIBLE', (SELECT id FROM hangar WHERE identifiant='H1')),
('HB-JCA', 'B737', 140, 'DISPONIBLE', (SELECT id FROM hangar WHERE identifiant='H2')),
('F-ZZZZ', 'A220', 120, 'DISPONIBLE', (SELECT id FROM hangar WHERE identifiant='H2')),
('N12345', 'G650', 12, 'DISPONIBLE', (SELECT id FROM hangar WHERE identifiant='A1')),
('F-AAAA', 'ATR72', 72, 'MAINTENANCE', (SELECT id FROM hangar WHERE identifiant='A1')),
('D-ABCD', 'A350', 320, 'MAINTENANCE', (SELECT id FROM hangar WHERE identifiant='A1')),
('F-QWER', 'B777', 380, 'EN_VOL', NULL),
('F-TTTT', 'A330', 260, 'EN_VOL', NULL),
('HB-XYZ', 'A319', 144, 'EN_VOL', NULL),
('N54321', 'C525', 6, 'EN_VOL', NULL),
('F-PLMA', 'A320', 180, 'DISPONIBLE', (SELECT id FROM hangar WHERE identifiant='B2')),
('F-BBB2', 'A318', 108, 'DISPONIBLE', (SELECT id FROM hangar WHERE identifiant='C3')),
('F-CCCC', 'A320', 180, 'DISPONIBLE', (SELECT id FROM hangar WHERE identifiant='C3')),
('F-DDDD', 'B787', 280, 'DISPONIBLE', (SELECT id FROM hangar WHERE identifiant='D4'));

-- ============================
-- VOLS (16)
-- ============================
INSERT INTO vol (
    numero_vol, origine, destination,
    heure_depart, heure_arrivee,
    etat, avion_id, piste_id
) VALUES
('AF201', 'ATL', 'CDG', '2025-02-01T09:00:00', '2025-02-01T18:00:00',
'PREVU', (SELECT id FROM avion WHERE immatriculation='F-ABCD'), NULL),

('AF203', 'ATL', 'LHR', '2025-02-01T11:00:00', '2025-02-01T19:00:00',
'EN_ATTENTE', (SELECT id FROM avion WHERE immatriculation='F-BCDE'), NULL),

('AF204', 'AMS', 'ATL', '2025-02-01T12:00:00', '2025-02-01T20:40:00',
'PREVU', NULL, NULL),

('AF205', 'ATL', 'MAD', '2025-02-01T10:00:00', '2025-02-01T17:30:00',
'EMBARQUEMENT', (SELECT id FROM avion WHERE immatriculation='HB-JCA'), (SELECT id FROM piste WHERE identifiant='09L')),

('AF206', 'FRA', 'ATL', '2025-02-01T13:00:00', '2025-02-01T22:10:00',
'DECOLLE', (SELECT id FROM avion WHERE immatriculation='F-ZZZZ'), (SELECT id FROM piste WHERE identifiant='09R')),

('AF207', 'ATL', 'BRU', '2025-02-01T14:00:00', '2025-02-02T00:10:00',
'EN_VOL', (SELECT id FROM avion WHERE immatriculation='F-QWER'), NULL),

('AF208', 'MXP', 'ATL', '2025-02-01T09:00:00', '2025-02-01T19:10:00',
'ARRIVE', (SELECT id FROM avion WHERE immatriculation='F-TTTT'), (SELECT id FROM piste WHERE identifiant='A3')),

('AF209', 'ATL', 'LUX', '2025-02-01T10:00:00', '2025-02-01T16:40:00',
'EN_VOL', (SELECT id FROM avion WHERE immatriculation='HB-XYZ'), NULL),

('AF210', 'OPO', 'ATL', '2025-02-01T12:00:00', '2025-02-01T21:20:00',
'EN_VOL', (SELECT id FROM avion WHERE immatriculation='N54321'), NULL),

('AF211', 'ATL', 'ATH', '2025-02-01T11:00:00', '2025-02-01T19:50:00',
'PREVU', (SELECT id FROM avion WHERE immatriculation='F-PLMA'), NULL),

('AF212', 'MAD', 'ATL', '2025-02-01T15:00:00', '2025-02-02T00:50:00',
'ANNULE', NULL, NULL),

('AF220', 'ATL', 'CDG', '2025-02-01T10:00:00', '2025-02-01T19:00:00',
'EN_ATTENTE', (SELECT id FROM avion WHERE immatriculation='F-BBB2'), NULL),

('AF221', 'ATL', 'CDG', '2025-02-01T11:00:00', '2025-02-01T20:00:00',
'PREVU', NULL, NULL),

('AF222', 'ATL', 'CDG', '2025-02-01T12:00:00', '2025-02-01T21:00:00',
'EMBARQUEMENT', (SELECT id FROM avion WHERE immatriculation='F-CCCC'), (SELECT id FROM piste WHERE identifiant='12L')),

('AF223', 'ATL', 'CDG', '2025-02-01T13:00:00', '2025-02-01T22:00:00',
'PREVU', (SELECT id FROM avion WHERE immatriculation='F-DDDD'), NULL),

('AF224', 'ATL', 'CDG', '2025-02-01T14:00:00', '2025-02-01T23:00:00',
'EN_ATTENTE', (SELECT id FROM avion WHERE immatriculation='N12345'), NULL);

-- ============================
-- PASSAGERS (15)
-- ============================
INSERT INTO passenger (prenom, nom, email, telephone) VALUES
('Jean', 'Dupont', 'jean.dupont@email.com', '+33612345678'),
('Marie', 'Martin', 'marie.martin@email.com', '+33698765432'),
('Pierre', 'Bernard', 'pierre.bernard@email.com', '+33611223344'),
('Sophie', 'Dubois', 'sophie.dubois@email.com', NULL),
('Luc', 'Thomas', 'luc.thomas@email.com', '+33645678901'),
('Emma', 'Robert', 'emma.robert@email.com', '+33656789012'),
('Antoine', 'Petit', 'antoine.petit@email.com', '+33667890123'),
('Julie', 'Richard', 'julie.richard@email.com', NULL),
('Marc', 'Durand', 'marc.durand@email.com', '+33678901234'),
('Claire', 'Moreau', 'claire.moreau@email.com', '+33689012345'),
('David', 'Laurent', 'david.laurent@email.com', '+33690123456'),
('Isabelle', 'Simon', 'isabelle.simon@email.com', NULL),
('François', 'Michel', 'francois.michel@email.com', '+33601234567'),
('Nathalie', 'Lefebvre', 'nathalie.lefebvre@email.com', '+33612345670'),
('Olivier', 'Leroy', 'olivier.leroy@email.com', NULL);

-- ============================
-- CHECK-INS (18)
-- ============================
INSERT INTO check_in (passenger_id, vol_id, numero_siege, heure_check_in) VALUES
((SELECT id FROM passenger WHERE email='jean.dupont@email.com'), (SELECT id FROM vol WHERE numero_vol='AF201'), '1A', '2025-02-01T06:00:00'),
((SELECT id FROM passenger WHERE email='marie.martin@email.com'), (SELECT id FROM vol WHERE numero_vol='AF201'), '1B', '2025-02-01T06:00:00'),
((SELECT id FROM passenger WHERE email='pierre.bernard@email.com'), (SELECT id FROM vol WHERE numero_vol='AF201'), '12C', '2025-02-01T07:00:00'),
((SELECT id FROM passenger WHERE email='sophie.dubois@email.com'), (SELECT id FROM vol WHERE numero_vol='AF203'), '5A', '2025-02-01T07:00:00'),
((SELECT id FROM passenger WHERE email='luc.thomas@email.com'), (SELECT id FROM vol WHERE numero_vol='AF203'), '5B', '2025-02-01T07:00:00'),
((SELECT id FROM passenger WHERE email='emma.robert@email.com'), (SELECT id FROM vol WHERE numero_vol='AF205'), '10F', '2025-02-01T08:00:00'),
((SELECT id FROM passenger WHERE email='antoine.petit@email.com'), (SELECT id FROM vol WHERE numero_vol='AF205'), '10E', '2025-02-01T08:00:00'),
((SELECT id FROM passenger WHERE email='julie.richard@email.com'), (SELECT id FROM vol WHERE numero_vol='AF205'), '15A', '2025-02-01T09:00:00'),
((SELECT id FROM passenger WHERE email='marc.durand@email.com'), (SELECT id FROM vol WHERE numero_vol='AF206'), '8C', '2025-02-01T10:00:00'),
((SELECT id FROM passenger WHERE email='claire.moreau@email.com'), (SELECT id FROM vol WHERE numero_vol='AF206'), '8D', '2025-02-01T10:00:00'),
((SELECT id FROM passenger WHERE email='david.laurent@email.com'), (SELECT id FROM vol WHERE numero_vol='AF207'), '25A', '2025-02-01T09:00:00'),
((SELECT id FROM passenger WHERE email='isabelle.simon@email.com'), (SELECT id FROM vol WHERE numero_vol='AF207'), '25B', '2025-02-01T09:00:00'),
((SELECT id FROM passenger WHERE email='francois.michel@email.com'), (SELECT id FROM vol WHERE numero_vol='AF211'), '20F', '2025-02-01T10:00:00'),
((SELECT id FROM passenger WHERE email='nathalie.lefebvre@email.com'), (SELECT id FROM vol WHERE numero_vol='AF220'), '7C', '2025-02-01T08:00:00'),
((SELECT id FROM passenger WHERE email='olivier.leroy@email.com'), (SELECT id FROM vol WHERE numero_vol='AF220'), '7D', '2025-02-01T08:00:00'),
((SELECT id FROM passenger WHERE email='jean.dupont@email.com'), (SELECT id FROM vol WHERE numero_vol='AF222'), '2A', '2025-02-01T10:00:00'),
((SELECT id FROM passenger WHERE email='marie.martin@email.com'), (SELECT id FROM vol WHERE numero_vol='AF222'), '2B', '2025-02-01T10:00:00'),
((SELECT id FROM passenger WHERE email='emma.robert@email.com'), (SELECT id FROM vol WHERE numero_vol='AF224'), '1C', '2025-02-01T13:00:00');
