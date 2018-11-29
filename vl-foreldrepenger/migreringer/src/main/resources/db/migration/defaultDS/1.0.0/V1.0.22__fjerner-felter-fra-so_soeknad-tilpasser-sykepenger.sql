ALTER TABLE SO_SOEKNAD DROP (medlemskap_oppg_tilknyt_id, fordeling_id, rettighet_id, DEKNINGSGRAD_ID);
ALTER TABLE SO_SOEKNAD ADD (SYKEFRAVAER_ID NUMBER (19),
  ARBEIDSGIVER_AKTOR_ID VARCHAR2(100 CHAR),
  ARBEIDSGIVER_VIRKSOMHET_ID NUMBER (19, 0),
  SOEKNAD_REFERANSE VARCHAR2(100 CHAR) NOT NULL,
  SYKEMELDING_REFERANSE VARCHAR2(100 CHAR) NOT NULL);

ALTER TABLE SO_SOEKNAD
  ADD CONSTRAINT FK_SO_SOEKNAD_01 FOREIGN KEY (ARBEIDSGIVER_VIRKSOMHET_ID) REFERENCES VIRKSOMHET;
ALTER TABLE SO_SOEKNAD
  ADD CONSTRAINT FK_SO_SOEKNAD_02 FOREIGN KEY (SYKEFRAVAER_ID) REFERENCES SF_SYKEFRAVAER;

COMMENT ON COLUMN SO_SOEKNAD.ARBEIDSGIVER_AKTOR_ID IS 'Arbeidsgivers aktørId hvis personlig foretak';
COMMENT ON COLUMN SO_SOEKNAD.SOEKNAD_REFERANSE IS 'Ekstern referanse for søknaden';
COMMENT ON COLUMN SO_SOEKNAD.SYKEMELDING_REFERANSE IS 'Ekstern referanse for sykemeldingen';

CREATE INDEX IDX_SO_SOEKNAD_01 ON SO_SOEKNAD (SYKEFRAVAER_ID);
CREATE INDEX IDX_SO_SOEKNAD_02 ON SO_SOEKNAD (ARBEIDSGIVER_VIRKSOMHET_ID);

INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'PAPIRSYKEMELDING', 'Papirsykemelding', to_date('2000-01-01', 'YYYY-MM-DD'),
        'SYKEFRAVÆR_PERIODE_TYPE');
INSERT INTO KODELISTE_NAVN_I18N (id, KL_KODE, KL_KODEVERK, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.nextval, 'PAPIRSYKEMELDING', 'SYKEFRAVÆR_PERIODE_TYPE', 'NB', 'Papirsykemelding');

INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'PERMISJON', 'Permisjon', to_date('2000-01-01', 'YYYY-MM-DD'),
        'SYKEFRAVÆR_PERIODE_TYPE');
INSERT INTO KODELISTE_NAVN_I18N (id, KL_KODE, KL_KODEVERK, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.nextval, 'PERMISJON', 'SYKEFRAVÆR_PERIODE_TYPE', 'NB', 'Permisjon');
INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'UTENLANDSOPPHOLD', 'Utenlandsopphold', to_date('2000-01-01', 'YYYY-MM-DD'),
        'SYKEFRAVÆR_PERIODE_TYPE');

INSERT INTO KODELISTE_NAVN_I18N (id, KL_KODE, KL_KODEVERK, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.nextval, 'UTENLANDSOPPHOLD', 'SYKEFRAVÆR_PERIODE_TYPE', 'NB', 'Utenlandsopphold');
INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'UTDANNING', 'Utdanning', to_date('2000-01-01', 'YYYY-MM-DD'),
        'SYKEFRAVÆR_PERIODE_TYPE');
INSERT INTO KODELISTE_NAVN_I18N (id, KL_KODE, KL_KODEVERK, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.nextval, 'UTDANNING', 'SYKEFRAVÆR_PERIODE_TYPE', 'NB', 'Utdanning');
INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'FERIE', 'Ferie', to_date('2000-01-01', 'YYYY-MM-DD'),
        'SYKEFRAVÆR_PERIODE_TYPE');
INSERT INTO KODELISTE_NAVN_I18N (id, KL_KODE, KL_KODEVERK, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.nextval, 'FERIE', 'SYKEFRAVÆR_PERIODE_TYPE', 'NB', 'Ferie');
