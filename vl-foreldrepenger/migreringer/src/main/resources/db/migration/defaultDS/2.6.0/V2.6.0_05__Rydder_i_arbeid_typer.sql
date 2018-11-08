DELETE FROM KODELISTE_RELASJON
WHERE KODEVERK1 = 'ARBEID_TYPE' AND KODE1 IN
                                    ('SYKEPENGER', 'DAGPENGER', 'AAP', 'FORELDREPENGER', 'SVANGERSKAPSPENGER', 'OPPLÆRINGSPENGER', 'OMSORGSPENGER', 'UTDANNINGSPERMISJON', 'PLEIEPENGER');
DELETE FROM KODELISTE_RELASJON
WHERE KODEVERK2 = 'ARBEID_TYPE' AND KODE2 IN
                                    ('SYKEPENGER', 'DAGPENGER', 'AAP', 'FORELDREPENGER', 'SVANGERSKAPSPENGER', 'OPPLÆRINGSPENGER', 'OMSORGSPENGER', 'UTDANNINGSPERMISJON', 'PLEIEPENGER');
DELETE FROM KODELISTE
WHERE KODEVERK = 'ARBEID_TYPE' AND KODE IN
                                   ('SYKEPENGER', 'DAGPENGER', 'AAP', 'FORELDREPENGER', 'SVANGERSKAPSPENGER', 'OPPLÆRINGSPENGER', 'OMSORGSPENGER', 'UTDANNINGSPERMISJON', 'PLEIEPENGER');

DELETE FROM AKTIVITETS_AVTALE
WHERE YRKESAKTIVITET_ID IN (SELECT id
                            FROM YRKESAKTIVITET
                            WHERE ARBEID_TYPE IN
                                  ('SYKEPENGER', 'DAGPENGER', 'AAP', 'FORELDREPENGER', 'SVANGERSKAPSPENGER', 'OPPLÆRINGSPENGER', 'OMSORGSPENGER', 'UTDANNINGSPERMISJON', 'PLEIEPENGER'));
DELETE FROM YRKESAKTIVITET
WHERE ARBEID_TYPE IN
      ('SYKEPENGER', 'DAGPENGER', 'AAP', 'FORELDREPENGER', 'SVANGERSKAPSPENGER', 'OPPLÆRINGSPENGER', 'OMSORGSPENGER', 'UTDANNINGSPERMISJON', 'PLEIEPENGER');
