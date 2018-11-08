-- HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OVERFOERING_ALENEOMSORG', 'Overføring: aleneomsorg', 'Overføring på grunn av aleneomsorg', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OVERFOERING_SKYDOM', 'Overføring: sykdom/skade', 'Overføring på grunn av sykdom/skade', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OVERFOERING_INNLEGGELSE', 'Overføring: innleggelse', 'Overføring på grunn av innleggelse', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'OVERFOERING_IKKE_RETT', 'Overføring: ikke rett', 'Overføring på grunn av ikke rett på foreldrepenger', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_AVKLART_SOEKNADSPERIODE_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'AVKLART_PERIODE', 'Avklart periode', 'Avklart periode', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_TYPE');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT', 'Sykdommen/skaden er dokumentert, angi avklart periode', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT', 'Innleggelsen er dokumentert, angi avklart periode', '', to_date('2000-01-01', 'YYYY-MM-DD'), 'HISTORIKK_ENDRET_FELT_VERDI_TYPE');
