INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('PERIODE_RESULTAT_TYPE', 'N', 'N', 'Internt kodeverk for perioderesultat', 'Internt kodeverk for perioderesultat.');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'INNVILGET', 'Innvilget', 'Innvilget', 'PERIODE_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'AVSLÅTT', 'Avslått', 'Avslått', 'PERIODE_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'IKKE_FASTSATT', 'Ikke fastsatt', 'Ikke fastsatt', 'PERIODE_RESULTAT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));


ALTER TABLE UTTAK_RESULTAT_PERIODE
ADD PERIODE_RESULTAT_TYPE    VARCHAR2(100 CHAR)                                  NOT NULL;

COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.PERIODE_RESULTAT_TYPE IS 'Resultat for perioden';
