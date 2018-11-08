-- NYE KODE FOR BEHANDLING RESULTAT TYPE
INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_RESULTAT_TYPE', 'FORELDREPENGER_ENDRET', 'Foreldrepenger er endret', 'Foreldrepenger er endret', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'BEHANDLING_RESULTAT_TYPE', 'INGEN_ENDRING', 'Ingen endring', 'Ingen endring', to_date('2000-01-01', 'YYYY-MM-DD'));

--NYTT KODEVERK RETTEN TIL
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('RETTEN_TIL', 'Brukers rett til foreldrepenger', 'Brukers rett til foreldrepenger','VL');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'RETTEN_TIL', 'HAR_RETT_TIL_FP', 'Bruker har rett til foreldrepenger', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'RETTEN_TIL', 'HAR_IKKE_RETT_TIL_FP', 'Bruker har ikke rett til foreldrepenger', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'RETTEN_TIL', '-', 'udefinert', to_date('2000-01-01', 'YYYY-MM-DD'));

--NYTT KODEVERK KONSEKVENS FOR YTELSEN
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('KONSEKVENS_FOR_YTELSEN', 'Konsekvens for ytelsen', 'Konsekvens for ytelsen','VL');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'KONSEKVENS_FOR_YTELSEN', 'FORELDREPENGER_OPPHØRER', 'Foreldrepenger opphører', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'KONSEKVENS_FOR_YTELSEN', 'ENDRING_I_BEREGNING', 'Endring i beregning', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'KONSEKVENS_FOR_YTELSEN', 'ENDRING_I_UTTAK', 'Endring i uttak', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'KONSEKVENS_FOR_YTELSEN', 'ENDRING_I_BEREGNING_OG_UTTAK', 'Endring i beregning og uttak', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'KONSEKVENS_FOR_YTELSEN', 'ENDRING_I_FORDELING_AV_YTELSEN', 'Endring i fordeling av ytelsen', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'KONSEKVENS_FOR_YTELSEN', 'INGEN_ENDRING', 'Ingen endring', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'KONSEKVENS_FOR_YTELSEN', '-', 'udefinert', to_date('2000-01-01', 'YYYY-MM-DD'));


--NYTT KODEVERK FOR VEDTAKSBREV
INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier) VALUES ('VEDTAKSBREV', 'Vedtaksbrev', 'Vedtaksbrev','VL');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VEDTAKSBREV', 'AUTOMATISK', 'Automatisk generert vedtaksbrev', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VEDTAKSBREV', 'INGEN', 'Ingen vedtaksbrev', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom)
VALUES(seq_kodeliste.nextval, 'VEDTAKSBREV', '-', 'udefinert', to_date('2000-01-01', 'YYYY-MM-DD'));

--BEHANDLING_RESULTAT KODEVERK CONSTRAINTS
ALTER TABLE BEHANDLING_RESULTAT ADD RETTEN_TIL  VARCHAR2(100 CHAR);
UPDATE BEHANDLING_RESULTAT SET RETTEN_TIL = '-';
ALTER TABLE BEHANDLING_RESULTAT MODIFY RETTEN_TIL NOT NULL;
ALTER TABLE BEHANDLING_RESULTAT ADD KL_RETTEN_TIL   VARCHAR2(100 CHAR)  AS ('RETTEN_TIL');

ALTER TABLE BEHANDLING_RESULTAT ADD KONSEKVENS_FOR_YTELSEN  VARCHAR2(100 CHAR);
UPDATE BEHANDLING_RESULTAT SET KONSEKVENS_FOR_YTELSEN = '-';
ALTER TABLE BEHANDLING_RESULTAT MODIFY KONSEKVENS_FOR_YTELSEN NOT NULL;
ALTER TABLE BEHANDLING_RESULTAT ADD KL_KONSEKVENS_FOR_YTELSEN   VARCHAR2(100 CHAR)  AS ('KONSEKVENS_FOR_YTELSEN');

ALTER TABLE BEHANDLING_RESULTAT ADD VEDTAKSBREV  VARCHAR2(100 CHAR);
UPDATE BEHANDLING_RESULTAT SET VEDTAKSBREV = '-';
ALTER TABLE BEHANDLING_RESULTAT MODIFY VEDTAKSBREV NOT NULL;
ALTER TABLE BEHANDLING_RESULTAT ADD KL_VEDTAKSBREV   VARCHAR2(100 CHAR)  AS ('VEDTAKSBREV');


COMMENT ON COLUMN BEHANDLING_RESULTAT.RETTEN_TIL IS 'FK: RETTEN_TIL';
COMMENT ON COLUMN BEHANDLING_RESULTAT.KL_RETTEN_TIL IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
COMMENT ON COLUMN BEHANDLING_RESULTAT.KONSEKVENS_FOR_YTELSEN IS 'FK: KONSEKVENS_FOR_YTELSEN';
COMMENT ON COLUMN BEHANDLING_RESULTAT.KL_KONSEKVENS_FOR_YTELSEN IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
COMMENT ON COLUMN BEHANDLING_RESULTAT.VEDTAKSBREV IS 'FK: VEDTAKSBREV';
COMMENT ON COLUMN BEHANDLING_RESULTAT.KL_VEDTAKSBREV IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
