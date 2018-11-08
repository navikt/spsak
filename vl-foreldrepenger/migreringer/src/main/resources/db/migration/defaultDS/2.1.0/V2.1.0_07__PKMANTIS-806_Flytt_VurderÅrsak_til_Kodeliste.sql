-- Modifisert tabell VURDER_PAA_NYTT_AARSAK
ALTER TABLE VURDER_PAA_NYTT_AARSAK
DROP CONSTRAINT FK_VURDER_PAA_NYTT_AARSAK_2;

ALTER TABLE VURDER_PAA_NYTT_AARSAK
ADD (KL_AARSAK_TYPE VARCHAR2(100 CHAR) AS ('VURDER_AARSAK'));

COMMENT ON COLUMN VURDER_PAA_NYTT_AARSAK.KL_AARSAK_TYPE IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';

ALTER TABLE VURDER_PAA_NYTT_AARSAK
MODIFY (KL_AARSAK_TYPE NOT NULL);

COMMENT ON COLUMN VURDER_PAA_NYTT_AARSAK.AARSAK_TYPE_ID IS 'Årsak for at aksjonspunkt må vurders på nytt';

ALTER TABLE VURDER_PAA_NYTT_AARSAK
RENAME COLUMN AARSAK_TYPE_ID to AARSAK_TYPE;

-- Fjern tabell VURDER_ARSAK
DROP TABLE VURDER_ARSAK;

--Flytt VURDER_AARSAK til kodeliste
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('VURDER_AARSAK', 'VurderÅrsak', 'Internt kodeverk for vurder årsak', 'VL', null, null, null, 'N', 'N');

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VURDER_AARSAK', 'FEIL_FAKTA', null, 'Feil fakta', 'Feil fakta', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VURDER_AARSAK', 'FEIL_LOV', null, 'Feil lovanvendelse', 'Feil lovanvendelse', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VURDER_AARSAK', 'FEIL_REGEL', null, 'Feil regelforståelse', 'Feil regelforståelse', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VURDER_AARSAK', 'ANNET', null, 'Annet', 'Annet', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VURDER_AARSAK', '-', null, 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'));

--Opprett Foreign key
ALTER TABLE VURDER_PAA_NYTT_AARSAK
ADD CONSTRAINT FK_VURDER_PAA_NYTT_AARSAK_2
FOREIGN KEY (AARSAK_TYPE, KL_AARSAK_TYPE) REFERENCES KODELISTE(KODE, KODEVERK);
