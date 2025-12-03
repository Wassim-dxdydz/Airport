-- ============================
-- HANGARS (8)
-- ============================
INSERT INTO hangar (identifiant, capacite, etat) VALUES
('H1', 5, 'DISPONIBLE'),
('H2', 4, 'DISPONIBLE'),
('H3', 3, 'DISPONIBLE'),
('H4', 3, 'DISPONIBLE'),
('A1', 5, 'DISPONIBLE'),
('B2', 4, 'DISPONIBLE'),
('C3', 3, 'DISPONIBLE'),
('D4', 2, 'DISPONIBLE');

-- ============================
-- PISTES (6)
-- ============================
INSERT INTO piste (identifiant, longueur_m, etat) VALUES
('09L', 3000, 'LIBRE'),
('12', 2800, 'LIBRE'),
('27R', 3200, 'LIBRE'),
('A1', 2500, 'LIBRE'),
('7B', 2900, 'LIBRE'),
('34', 3100, 'LIBRE');

-- ============================
-- AVIONS (15)
-- ============================
INSERT INTO avion (immatriculation, type, capacite, etat, hangar_id) VALUES
('F-ABCD', 'A320', 180, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='H1')),
('F-BCDE', 'A320', 180, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='H1')),
('F-CDEF', 'B737', 200, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='H2')),
('F-DEFG', 'B737', 200, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='H2')),
('HB-JCA', 'A330', 250, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='H3')),
('HB-JCB', 'A330', 250, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='H3')),
('D-ABCD', 'A350', 300, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='H4')),
('D-BCDE', 'A350', 300, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='H4')),
('N12345', 'B777', 320, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='A1')),
('N1A2B3', 'B777', 320, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='A1')),
('F-ZYXA', 'A321', 190, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='B2')),
('F-ZYXB', 'A321', 190, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='B2')),
('HB-AA11', 'B787', 280, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='C3')),
('HB-AC22', 'B787', 280, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='C3')),
('N4321A', 'A319', 150, 'EN_SERVICE', (SELECT id FROM hangar WHERE identifiant='D4'));

-- ============================
-- VOLS (15)
-- ============================
INSERT INTO vol (
    numero_vol, origine, destination,
    heure_depart, heure_arrivee,
    etat, avion_id, piste_id
) VALUES
      ('V001', 'CDG', 'LHR', '2025-01-01T08:00:00', '2025-01-01T09:10:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='F-ABCD'), (SELECT id FROM piste WHERE identifiant='09L')),

      ('V002', 'LHR', 'CDG', '2025-01-01T11:00:00', '2025-01-01T12:20:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='F-BCDE'), (SELECT id FROM piste WHERE identifiant='12')),

      ('V003', 'MAD', 'CDG', '2025-01-02T07:30:00', '2025-01-02T09:50:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='F-CDEF'), (SELECT id FROM piste WHERE identifiant='27R')),

      ('V004', 'BCN', 'FRA', '2025-01-02T10:00:00', '2025-01-02T12:00:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='F-DEFG'), (SELECT id FROM piste WHERE identifiant='A1')),

      ('V005', 'FRA', 'IST', '2025-01-03T06:20:00', '2025-01-03T10:00:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='HB-JCA'), (SELECT id FROM piste WHERE identifiant='7B')),

      ('V006', 'IST', 'ALG', '2025-01-03T13:00:00', '2025-01-03T15:40:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='HB-JCB'), (SELECT id FROM piste WHERE identifiant='34')),

      ('V007', 'ALG', 'CDG', '2025-01-04T09:00:00', '2025-01-04T11:30:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='D-ABCD'), (SELECT id FROM piste WHERE identifiant='09L')),

      ('V008', 'CDG', 'MAD', '2025-01-04T14:00:00', '2025-01-04T16:10:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='D-BCDE'), (SELECT id FROM piste WHERE identifiant='12')),

      ('V009', 'ORY', 'BCN', '2025-01-05T07:00:00', '2025-01-05T08:50:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='N12345'), (SELECT id FROM piste WHERE identifiant='27R')),

      ('V010', 'BCN', 'ORY', '2025-01-05T10:00:00', '2025-01-05T11:50:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='N1A2B3'), (SELECT id FROM piste WHERE identifiant='A1')),

      ('V011', 'LYS', 'CDG', '2025-01-06T06:30:00', '2025-01-06T07:30:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='F-ZYXA'), (SELECT id FROM piste WHERE identifiant='7B')),

      ('V012', 'CDG', 'LYS', '2025-01-06T09:00:00', '2025-01-06T10:00:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='F-ZYXB'), (SELECT id FROM piste WHERE identifiant='34')),

      ('V013', 'ROM', 'CDG', '2025-01-07T12:00:00', '2025-01-07T14:00:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='HB-AA11'), (SELECT id FROM piste WHERE identifiant='09L')),

      ('V014', 'CDG', 'ROM', '2025-01-07T16:00:00', '2025-01-07T18:00:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='HB-AC22'), (SELECT id FROM piste WHERE identifiant='12')),

      ('V015', 'NCE', 'MAD', '2025-01-08T08:00:00', '2025-01-08T10:00:00',
       'PREVU', (SELECT id FROM avion WHERE immatriculation='N4321A'), (SELECT id FROM piste WHERE identifiant='27R'));

