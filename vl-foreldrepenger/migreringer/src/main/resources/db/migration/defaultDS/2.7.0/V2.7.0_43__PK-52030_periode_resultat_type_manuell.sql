INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'MANUELL_BEHANDLING', 'Til manuell behandling', 'Perioden må behandles manuelt', 'PERIODE_RESULTAT_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));
