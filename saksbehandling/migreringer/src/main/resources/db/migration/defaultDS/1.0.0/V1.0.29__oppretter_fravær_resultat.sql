CREATE SEQUENCE SEQ_FR_FRAVAER_PERIODER
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NO CYCLE;

CREATE TABLE FR_FRAVAER_PERIODER
(
  id            bigint                              NOT NULL,
  versjon       bigint       DEFAULT 0              NOT NULL,
  opprettet_av  VARCHAR(20)  DEFAULT 'VL'           NOT NULL,
  opprettet_tid TIMESTAMP(3) DEFAULT localtimestamp NOT NULL,
  endret_av     VARCHAR(20),
  endret_tid    TIMESTAMP(3),
  CONSTRAINT PK_FR_FRAVAER_PERIODER PRIMARY KEY (id)
);

COMMENT ON TABLE FR_FRAVAER_PERIODER
  IS 'Mange-til-mange holder for resultat perioder';

CREATE SEQUENCE SEQ_FR_FRAVAER_PERIODE
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NO CYCLE;

CREATE TABLE FR_FRAVAER_PERIODE
(
  id                         bigint                              NOT NULL,
  versjon                    bigint       DEFAULT 0              NOT NULL,
  perioder_id                bigint                              NOT NULL,
  ARBEIDSGIVER_AKTOR_ID      VARCHAR(100),
  ARBEIDSGIVER_VIRKSOMHET_ID bigint,
  gradering                  VARCHAR(1)   DEFAULT 'N'            NOT NULL,
  graderings_prosent         NUMERIC(5, 2)                       NOT NULL,
  FOM                        DATE                                NOT NULL,
  TOM                        DATE                                NOT NULL,
  opprettet_av               VARCHAR(20)  DEFAULT 'VL'           NOT NULL,
  opprettet_tid              TIMESTAMP(3) DEFAULT localtimestamp NOT NULL,
  endret_av                  VARCHAR(20),
  endret_tid                 TIMESTAMP(3),
  CONSTRAINT PK_FR_FRAVAER_PERIODE PRIMARY KEY (id),
  CONSTRAINT FK_FR_FRAVAER_PERIODE_1 FOREIGN KEY (perioder_id) REFERENCES FR_FRAVAER_PERIODER,
  CONSTRAINT FK_FR_FRAVAER_PERIODE_2 FOREIGN KEY (ARBEIDSGIVER_VIRKSOMHET_ID) REFERENCES VIRKSOMHET,
  CONSTRAINT CHK_FR_FRAVAER_PERIODE_1 CHECK (gradering IN ('J', 'N'))
);

COMMENT ON TABLE FR_FRAVAER_PERIODE
  IS 'Resultat periode';
COMMENT ON COLUMN FR_FRAVAER_PERIODE.ARBEIDSGIVER_AKTOR_ID
  IS 'Arbeidsgivers aktørId hvis personlig foretak';
COMMENT ON COLUMN FR_FRAVAER_PERIODE.gradering
  IS 'Indikerer om perioden er gradert';
COMMENT ON COLUMN FR_FRAVAER_PERIODE.graderings_prosent
  IS 'Andelen gradering';

CREATE SEQUENCE SEQ_RES_FRAVAER
  MINVALUE 1
  START WITH 1
  INCREMENT BY 50
  NO CYCLE;

CREATE TABLE RES_FRAVAER
(
  id                    bigint                              NOT NULL,
  behandlingresultat_id bigint                              NOT NULL,
  perioder_id           bigint                              NOT NULL,
  aktiv                 VARCHAR(1)   DEFAULT 'N'            NOT NULL,
  versjon               bigint       DEFAULT 0              NOT NULL,
  opprettet_av          VARCHAR(20)  DEFAULT 'VL'           NOT NULL,
  opprettet_tid         TIMESTAMP(3) DEFAULT localtimestamp NOT NULL,
  endret_av             VARCHAR(20),
  endret_tid            TIMESTAMP(3),
  CONSTRAINT PK_RES_FRAVAER PRIMARY KEY (id),
  CONSTRAINT FK_RES_FRAVAER_1 FOREIGN KEY (behandlingresultat_id) REFERENCES behandling_resultat,
  CONSTRAINT FK_RES_FRAVAER_2 FOREIGN KEY (perioder_id) REFERENCES FR_FRAVAER_PERIODER,
  CONSTRAINT CHK_RES_FRAVAER_1 CHECK (AKTIV IN ('J', 'N'))
);
COMMENT ON TABLE RES_FRAVAER
  IS 'Resultat struktur for perioder';

CREATE UNIQUE INDEX UIDX_RES_FRAVAER_01
  ON RES_FRAVAER (
                  (CASE
                     WHEN AKTIV = 'J'
                       THEN behandlingresultat_id
                     ELSE NULL END),
                  (CASE
                     WHEN AKTIV = 'J'
                       THEN AKTIV
                     ELSE NULL END)
    );
