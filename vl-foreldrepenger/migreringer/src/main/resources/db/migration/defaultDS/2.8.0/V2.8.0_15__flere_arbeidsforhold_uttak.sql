update UTTAK_RESULTAT_PERIODE set ARBFORHOLD_ORGNR = null, ARBFORHOLD_ID = null where ARBFORHOLD_ID = 'arbeidsforholdId';

INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('UTTAK_ARBEID_TYPE', 'N', 'N', 'Uttak arbeidtype', 'Internt kodeverk for typene av arbeid');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ORDINÆRT_ARBEID', 'Ordinært arbeid', 'Ordinært arbeid', 'UTTAK_ARBEID_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'ANNET', 'Annet', 'Annet', 'UTTAK_ARBEID_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

ALTER TABLE UTTAK_RESULTAT_PERIODE ADD UTTAK_ARBEID_TYPE VARCHAR2(100 CHAR) DEFAULT 'ANNET' NOT NULL;
ALTER TABLE UTTAK_RESULTAT_PERIODE ADD KL_UTTAK_ARBEID_TYPE VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('UTTAK_ARBEID_TYPE') VIRTUAL;

ALTER TABLE UTTAK_RESULTAT_PERIODE ADD CONSTRAINT FK_UTTAK_RESULTAT_PERIODE_08 FOREIGN KEY (UTTAK_ARBEID_TYPE, KL_UTTAK_ARBEID_TYPE)
REFERENCES KODELISTE (KODE, KODEVERK);

CREATE INDEX IDX_UTTAK_RESULTAT_PERIODE_8 ON UTTAK_RESULTAT_PERIODE(UTTAK_ARBEID_TYPE);

COMMENT ON COLUMN UTTAK_RESULTAT_PERIODE.UTTAK_ARBEID_TYPE IS 'Type arbeid i periode';

update UTTAK_RESULTAT_PERIODE set UTTAK_ARBEID_TYPE = 'ORDINÆRT_ARBEID' where ARBFORHOLD_ID is not null;
