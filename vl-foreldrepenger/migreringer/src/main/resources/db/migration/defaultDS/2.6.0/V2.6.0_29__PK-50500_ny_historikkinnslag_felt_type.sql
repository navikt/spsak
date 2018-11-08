INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'GJELDENDE_FRA', 'Gjeldende fra', 'Endringer gjeldende fra', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKKINNSLAG_FELT_TYPE');
