INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, NAVN, BESKRIVELSE, SPRAK, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV, OPPRETTET_TID)
VALUES (nextval('seq_kodeliste'), 'BEHANDLING_TEMA', 'SVAP', 'ab0126', 'Svangerskapspenger',
                               'Svangerskapspenger', 'NB', to_date('01.07.2006', 'DD.MM.YYYY'),
                               to_date('31.12.9999', 'DD.MM.YYYY'), 'VL', current_timestamp);
