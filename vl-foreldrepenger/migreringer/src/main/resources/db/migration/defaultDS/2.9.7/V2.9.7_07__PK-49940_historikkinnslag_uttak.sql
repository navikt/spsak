-- HISTORIKK_ENDRET_FELT_VERDI_TYPE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FASTSETT_RESULTAT_ENDRE_SOEKNADSPERIODEN', 'Endre søknadsperioden', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');

-- HISTORIKK_ENDRET_FELT_TYPE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ANDEL_ARBEID', 'Andel i arbeid', 'Andel i arbeid', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');

-- HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'UTTAK', 'Uttak', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');
