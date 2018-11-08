ALTER TABLE AKSJONSPUNKT
DROP CONSTRAINT FK_AKSJONSPUNKT_5;

ALTER TABLE AKSJONSPUNKT
ADD (KL_VENT_AARSAK VARCHAR2(100 CHAR) AS ('VENT_AARSAK'));

COMMENT ON COLUMN AKSJONSPUNKT.KL_VENT_AARSAK IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';

ALTER TABLE AKSJONSPUNKT
MODIFY (KL_VENT_AARSAK NOT NULL);

UPDATE AKSJONSPUNKT
SET VENT_AARSAK='-'
WHERE VENT_AARSAK IS NULL;

ALTER TABLE AKSJONSPUNKT
MODIFY VENT_AARSAK DEFAULT '-';

ALTER TABLE AKSJONSPUNKT
MODIFY (VENT_AARSAK NOT NULL);

DROP TABLE VENT_AARSAK;

INSERT INTO kodeverk (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_eier_navn, kodeverk_synk_nye, kodeverk_synk_eksisterende)
VALUES ('VENT_AARSAK', 'Venteårsak', 'Internt kodeverk for vente årsak', 'VL', null, null, null, 'N', 'N');

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VENT_AARSAK', 'AVV_DOK', null, 'Avventer dokumentasjon', 'Avventer dokumentasjon', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VENT_AARSAK', 'AVV_FODSEL', null, 'Avventer fødsel', 'Avventer fødsel', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VENT_AARSAK', 'UTV_FRIST', null, 'Utvidet frist', 'Bruker har bedt om utvidet frist', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VENT_AARSAK', 'SCANN', null, 'Venter på scanning', 'Venter på scanning', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VENT_AARSAK', '-', null, 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'));

ALTER TABLE AKSJONSPUNKT
ADD CONSTRAINT FK_AKSJONSPUNKT_5
FOREIGN KEY (VENT_AARSAK, KL_VENT_AARSAK) REFERENCES KODELISTE(KODE, KODEVERK);
