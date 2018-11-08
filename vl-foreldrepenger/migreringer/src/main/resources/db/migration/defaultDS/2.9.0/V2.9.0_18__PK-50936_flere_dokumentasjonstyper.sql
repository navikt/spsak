INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_DOKUMENTASJON_TYPE', 'INSTITUSJONSOPPHOLD_ANNEN_FORELDRE', 'Annen forelder er innlagt i institusjon',
        'Det er dokumentert at annen forelder er innlagt', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'UTTAK_DOKUMENTASJON_TYPE', 'SYKDOM_ANNEN_FORELDER', 'Annen forelder er syk eller skadet',
        'Det er dokumentert at annen forelder er syk eller skadet', to_date('2000-01-01', 'YYYY-MM-DD'));
