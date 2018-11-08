INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('UTTAK_UTSETTELSE_TYPE', 'Kodeverk over årsaker til avvik(Utsettelse, opphold eller overføring) i perioder.', '', 'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, '-', 'Ikke satt eller valgt kode', 'Ikke satt eller valgt kode', to_date('2000-01-01', 'YYYY-MM-DD'), 'UTTAK_UTSETTELSE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'FERIE', 'Lovbestemt ferie', 'Lovbestemt ferie', to_date('2000-01-01', 'YYYY-MM-DD'), 'UTTAK_UTSETTELSE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'SYKDOM_SKADE', 'Avhengig av hjelp grunnet sykdom', 'Avhengig av hjelp grunnet sykdom', to_date('2000-01-01', 'YYYY-MM-DD'), 'UTTAK_UTSETTELSE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'ARBEID', 'Arbeid', 'Arbeid', to_date('2000-01-01', 'YYYY-MM-DD'), 'UTTAK_UTSETTELSE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'SØKER_INNLAGT', 'Søker er innlagt i helseinstitusjon', 'Søker er innlagt i helseinstitusjon', to_date('2000-01-01', 'YYYY-MM-DD'), 'UTTAK_UTSETTELSE_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk) VALUES (seq_kodeliste.nextval, 'BARN_INNLAGT', 'Barn er innlagt i helseinstitusjon', 'Barn er innlagt i helseinstitusjon', to_date('2000-01-01', 'YYYY-MM-DD'), 'UTTAK_UTSETTELSE_TYPE');

ALTER TABLE UTTAK_RESULTAT_PERIODE ADD UTTAK_UTSETTELSE_TYPE VARCHAR2(100 CHAR) NOT NULL;

COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.UTTAK_UTSETTELSE_TYPE IS 'Om utsettelse er typen til perioden';
