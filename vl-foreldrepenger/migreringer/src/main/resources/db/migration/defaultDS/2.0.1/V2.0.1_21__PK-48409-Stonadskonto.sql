/* STØNADSKONTOTYPE */
INSERT INTO KODEVERk (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('STOENADSKONTOTYPE', 'N', 'N', 'Stønadskontotype', 'Internt kodeverk for typene av stønadskontoer som finnes. Feks mødrekvote, fedrevote og fellesperiode.');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'FELLESPERIODE', 'Fellesperiode', 'Fellesperiode', 'STOENADSKONTOTYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'MØDREKVOTE', 'Mødrekvote', 'Mødrekvote', 'STOENADSKONTOTYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'FEDREKVOTE', 'Fedrekvote', 'Fedrekvote', 'STOENADSKONTOTYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'FORELDREPENGER', 'Foreldrepenger', 'Foreldrepenger', 'STOENADSKONTOTYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
VALUES (seq_kodeliste.nextval, 'SAMTIDIGUTTAK', 'Samtidig uttak', 'Del av fellesperiode som kan tas ut samtidig av begge foreldrene. Ekstra uker som gis ved fødsel/adopsjon av flere enn 1 barn. ', 'STOENADSKONTOTYPE', to_date('2000-01-01', 'YYYY-MM-DD'));

CREATE TABLE STOENADSKONTOBEREGNING (
  ID                        NUMBER(19, 0)                                       NOT NULL,
  FAGSAK_RELASJON_ID        NUMBER(19, 0)                                       NOT NULL,
  BEHANDLING_ID             NUMBER(19, 0)                                       NOT NULL,
  REGEL_INPUT               CLOB                                                NOT NULL,
  REGEL_EVALUERING          CLOB                                                NOT NULL,
  OPPRETTET_AV              VARCHAR2(20 CHAR) DEFAULT 'VL'                      NOT NULL,
  OPPRETTET_TID             TIMESTAMP(3)      DEFAULT systimestamp              NOT NULL,
  ENDRET_AV                 VARCHAR2(20 CHAR),
  ENDRET_TID                TIMESTAMP(3),
  CONSTRAINT PK_STOENADSKONTOBEREGNING PRIMARY KEY (ID),
  CONSTRAINT FK_STOENADSKONTOBEREGNING_1 FOREIGN KEY (FAGSAK_RELASJON_ID) REFERENCES FAGSAK_RELASJON(ID),
  CONSTRAINT FK_STOENADSKONTOBEREGNING_2 FOREIGN KEY (BEHANDLING_ID) REFERENCES BEHANDLING(ID)
);

CREATE SEQUENCE SEQ_STOENADSKONTOBEREGNING MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE STOENADSKONTOBEREGNING IS 'Grunnlag og vurdering som ligger til grunn for de beregnede støndadskontoene.';
COMMENT ON COLUMN STOENADSKONTOBEREGNING.REGEL_INPUT IS 'Grunnlaget som var input for beregning av stønadskontoene.';
COMMENT ON COLUMN STOENADSKONTOBEREGNING.REGEL_EVALUERING IS 'Sporing av beregningen av stønadskontoene.';

CREATE TABLE STOENADSKONTO (
  ID                        NUMBER(19, 0)                                       NOT NULL,
  MAX_DAGER                 NUMBER(3,0)                                         NOT NULL,
  STOENADSKONTOTYPE         VARCHAR2(100 CHAR)                                  NOT NULL,
  STOENADSKONTOBEREGNING_ID NUMBER(19, 0)                                       NOT NULL,
  KL_STOENADSKONTOTYPE      VARCHAR2(100 CHAR) DEFAULT 'STOENADSKONTOTYPE'      NOT NULL,
  OPPRETTET_AV              VARCHAR2(20 CHAR) DEFAULT 'VL'                      NOT NULL,
  OPPRETTET_TID             TIMESTAMP(3)      DEFAULT systimestamp              NOT NULL,
  ENDRET_AV                 VARCHAR2(20 CHAR),
  ENDRET_TID                TIMESTAMP(3),
  CONSTRAINT PK_STOENADSKONTO PRIMARY KEY (ID),
  CONSTRAINT FK_STOENADSKONTO_1 FOREIGN KEY (STOENADSKONTOBEREGNING_ID) REFERENCES STOENADSKONTOBEREGNING(ID)
);

CREATE SEQUENCE SEQ_STOENADSKONTO MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE STOENADSKONTO IS 'Beregnede stønadskontoer som angir maks antall dager man kan ta ut per konto.';
COMMENT ON COLUMN STOENADSKONTO.MAX_DAGER IS 'Max antall virkedager det er mulig å ta ut på denne stønadskontoen';
COMMENT ON COLUMN STOENADSKONTO.STOENADSKONTOTYPE IS 'Stønadskontotype fra internt kodeverk.';



